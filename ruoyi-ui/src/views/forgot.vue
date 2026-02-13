<template>
  <div class="forgot-container">
    <div class="forgot-box">
      <div class="forgot-header">
        <div class="forgot-title">找回密码</div>
        <div class="forgot-subtitle">重置您的登录密码</div>
      </div>
      
      <el-steps :active="activeStep" finish-status="success" align-center class="forgot-steps">
        <el-step title="验证账号" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>

      <!-- Step 1: Verify Account -->
      <el-form v-if="activeStep === 0" ref="verifyRef" :model="verifyForm" :rules="verifyRules" label-width="0" class="forgot-form">
        <el-form-item prop="username">
          <el-input v-model="verifyForm.username" type="text" placeholder="请输入账号" size="large">
            <template #prefix><el-icon class="input-icon"><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="verifyForm.email" type="text" placeholder="请输入注册邮箱" size="large">
            <template #prefix><el-icon class="input-icon"><Message /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="code">
          <div class="captcha-container">
            <el-input v-model="verifyForm.code" placeholder="验证码" size="large" class="captcha-input">
              <template #prefix><el-icon class="input-icon"><Key /></el-icon></template>
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </div>
        </el-form-item>
        <el-button type="primary" size="large" class="next-btn" @click="handleVerify">下一步</el-button>
        <div class="back-link">
          <el-link type="info" @click="handleBack">返回登录</el-link>
        </div>
      </el-form>

      <!-- Step 2: Reset Password -->
      <el-form v-if="activeStep === 1" ref="resetRef" :model="resetForm" :rules="resetRules" label-width="0" class="forgot-form">
        <el-form-item prop="password">
          <el-input v-model="resetForm.password" type="password" placeholder="请输入新密码" size="large" show-password>
            <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请确认新密码" size="large" show-password>
            <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" class="next-btn" @click="handleReset">重置密码</el-button>
        <div class="back-link">
          <el-link type="info" @click="activeStep = 0">上一步</el-link>
        </div>
      </el-form>

      <!-- Step 3: Success -->
      <div v-if="activeStep === 2" class="success-step">
        <el-result icon="success" title="密码重置成功" sub-title="请使用新密码重新登录系统">
          <template #extra>
            <el-button type="primary" size="medium" @click="handleBack">立即登录</el-button>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login";
import { User, Message, Key, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter();
const { proxy } = getCurrentInstance();

const activeStep = ref(0);
const codeUrl = ref("");
const verifyForm = ref({
  username: "",
  email: "",
  code: "",
  uuid: ""
});
const resetForm = ref({
  password: "",
  confirmPassword: ""
});

const verifyRules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }
  ],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }]
};

const resetRules = {
  password: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 5, max: 20, message: "长度在 5 到 20 个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.value.password) {
          callback(new Error("两次输入密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
};

function getCode() {
  getCodeImg().then(res => {
    codeUrl.value = "data:image/gif;base64," + res.img;
    verifyForm.value.uuid = res.uuid;
  });
}

function handleVerify() {
  proxy.$refs.verifyRef.validate(valid => {
    if (valid) {
      // Mock verification logic
      if (verifyForm.value.username === "admin") {
         activeStep.value = 1;
      } else {
         ElMessage.error("账号或邮箱验证失败（模拟环境仅支持admin账号）");
         getCode();
      }
    }
  });
}

function handleReset() {
  proxy.$refs.resetRef.validate(valid => {
    if (valid) {
      // Mock reset logic
      setTimeout(() => {
        activeStep.value = 2;
      }, 1000);
    }
  });
}

function handleBack() {
  router.push("/login");
}

getCode();
</script>

<style lang="scss" scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
  background-image: url("@/assets/images/login-background.jpg");
  background-size: cover;
}

.forgot-box {
  width: 500px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;
  
  .forgot-title {
    font-size: 24px;
    font-weight: bold;
    color: #1e3a8a;
    margin-bottom: 10px;
  }
  
  .forgot-subtitle {
    font-size: 14px;
    color: #6b7280;
  }
}

.forgot-steps {
  margin-bottom: 40px;
}

.forgot-form {
  .captcha-container {
    display: flex;
    justify-content: space-between;
    width: 100%;
    
    .captcha-input {
      flex: 1;
      margin-right: 15px;
    }
    
    .login-code {
      width: 120px;
      height: 40px;
      border-radius: 4px;
      overflow: hidden;
      border: 1px solid #dcdfe6;
      cursor: pointer;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
  
  .next-btn {
    width: 100%;
    margin-top: 10px;
    font-size: 16px;
    background: linear-gradient(to right, #1e40af, #3b82f6);
    border: none;
    
    &:hover {
      opacity: 0.9;
    }
  }
  
  .back-link {
    text-align: center;
    margin-top: 20px;
  }
}

.success-step {
  text-align: center;
}
</style>
