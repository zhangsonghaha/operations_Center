<template>
  <el-dialog :title="title" v-model="open" width="800px" append-to-body>
    <div v-loading="loading" class="doc-preview-container">
      <!-- PDF Preview -->
      <iframe v-if="docType === 'pdf'" :src="previewUrl" width="100%" height="600px" frameborder="0"></iframe>
      
      <!-- Word Preview -->
      <div v-else-if="['doc', 'docx'].includes(docType)" class="markdown-body" v-html="wordHtml"></div>
      
      <!-- Markdown Preview -->
      <div v-else-if="docType === 'md'" class="markdown-body" v-html="markdownHtml"></div>
      
      <!-- Other -->
      <div v-else class="text-center">
        <p>暂不支持在线预览该格式，请下载查看。</p>
        <el-button type="primary" icon="Download" @click="handleDownload">下载文件</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import mammoth from "mammoth/mammoth.browser";
import { marked } from 'marked';
import { getToken } from "@/utils/auth";

export default {
  name: "DocPreview",
  data() {
    return {
      title: "文档预览",
      open: false,
      loading: false,
      docType: "",
      previewUrl: "",
      wordHtml: "",
      markdownHtml: "",
      currentDoc: null
    };
  },
  methods: {
    preview(doc) {
      this.currentDoc = doc;
      this.title = `预览 - ${doc.docName}`;
      this.docType = doc.docType.toLowerCase();
      this.open = true;
      this.loading = true;
      
      const baseUrl = import.meta.env.VITE_APP_BASE_API;
      // Use resource download URL for fetching content
      const fileUrl = baseUrl + "/common/download/resource?resource=" + encodeURIComponent(doc.docPath);
      
      if (this.docType === 'pdf') {
        this.fetchBlob(fileUrl).then(blob => {
          // 强制指定 PDF 类型
          const pdfBlob = new Blob([blob], { type: 'application/pdf' });
          this.previewUrl = URL.createObjectURL(pdfBlob);
          this.loading = false;
        }).catch(err => {
          console.error("PDF预览失败", err);
          this.$message.error("预览失败: " + err.message);
          this.loading = false;
        });
      } else if (['doc', 'docx'].includes(this.docType)) {
        this.fetchArrayBuffer(fileUrl).then(arrayBuffer => {
          mammoth.convertToHtml({ arrayBuffer: arrayBuffer })
            .then(result => {
              this.wordHtml = result.value;
              this.loading = false;
            })
            .catch(err => {
              this.$message.error("Word预览失败: " + err.message);
              this.loading = false;
            });
        });
      } else if (this.docType === 'md') {
        this.fetchText(fileUrl).then(text => {
          this.markdownHtml = marked.parse(text);
          this.loading = false;
        });
      } else {
        this.loading = false;
      }
    },
    fetchBlob(url) {
      return fetch(url, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      }).then(async res => {
        if (!res.ok) {
          throw new Error(`请求失败: ${res.status}`);
        }
        const contentType = res.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
          const json = await res.json();
          throw new Error(json.msg || '文件获取失败');
        }
        return res.blob();
      });
    },
    fetchArrayBuffer(url) {
      return fetch(url, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      }).then(res => res.arrayBuffer());
    },
    fetchText(url) {
      return fetch(url, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
      }).then(res => res.text());
    },
    handleDownload() {
      this.$download.resource(this.currentDoc.docPath);
    }
  }
};
</script>

<style scoped>
.doc-preview-container {
  min-height: 400px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 20px;
}
.markdown-body {
  box-sizing: border-box;
  min-width: 200px;
  max-width: 980px;
  margin: 0 auto;
  padding: 45px;
}
</style>
