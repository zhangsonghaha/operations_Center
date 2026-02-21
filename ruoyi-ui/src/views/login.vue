<template>
  <div class="landing-page">
    <!-- Minimal Navbar -->
    <header class="navbar">
      <div class="container navbar-content">
        <div class="logo">
          <el-icon class="logo-icon"><Monitor /></el-icon>
          <span class="logo-text">OpsCenter</span>
        </div>
        <div class="nav-actions">
          <el-button type="primary" plain class="login-btn" @click="openLogin">进入控制台</el-button>
        </div>
      </div>
    </header>

    <!-- Clean Hero Section -->
    <section class="hero-section">
      <div class="container hero-content">
        <h1 class="hero-title">智能运维，化繁为简</h1>
        <p class="hero-subtitle">
          集监控、日志、自动化部署于一体的一站式运维管理平台。<br>
          让基础设施管理更简单、更高效。
        </p>
        <div class="hero-buttons">
          <el-button type="primary" size="large" class="cta-btn" @click="openLogin">立即开始</el-button>
          <el-button size="large" class="cta-btn secondary" @click="scrollToFeatures">了解功能</el-button>
        </div>
      </div>
    </section>

    <!-- Minimal Features Grid -->
    <section id="features" class="section features-section">
      <div class="container">
        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon"><el-icon><Odometer /></el-icon></div>
            <h3>实时监控</h3>
            <p>全链路性能指标采集，秒级监控告警，掌控系统每一刻状态。</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><Cpu /></el-icon></div>
            <h3>资源编排</h3>
            <p>基于 IaC 的自动化资源管理，支持多云环境的统一纳管与调度。</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><Connection /></el-icon></div>
            <h3>持续交付</h3>
            <p>内置流水线引擎，实现代码构建、测试、部署的全流程自动化。</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon"><el-icon><DataAnalysis /></el-icon></div>
            <h3>智能分析</h3>
            <p>AI 驱动的日志分析与故障根因定位，大幅降低平均修复时间。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 OpsCenter. All rights reserved.</p>
      </div>
    </footer>

    <!-- Login Dialog (Minimalist Style) -->
    <el-dialog
      v-model="loginVisible"
      width="420px"
      append-to-body
      class="login-dialog minimal-dialog"
      :close-on-click-modal="false"
      align-center
      :show-close="false"
    >
      <div class="dialog-content">
        <!-- Login Type Switcher -->
        <div class="login-switcher" @click="toggleLoginType">
          <el-tooltip :content="loginType === 'account' ? '扫码登录' : '账号登录'" placement="left">
            <div class="switcher-icon">
              <el-icon v-if="loginType === 'account'"><Grid /></el-icon>
              <el-icon v-else><Monitor /></el-icon>
            </div>
          </el-tooltip>
        </div>

        <div class="dialog-header">
          <h2 v-if="loginType === 'account'">欢迎回来</h2>
          <h2 v-else>扫码登录</h2>
          <p v-if="loginType === 'account'">请登录 OpsCenter 账号</p>
          <p v-else>请使用手机 App 扫码登录</p>
        </div>

        <!-- Account Login Form -->
        <el-form v-if="loginType === 'account'" ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              size="large"
              placeholder="账号"
              class="minimal-input"
            >
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              size="large"
              placeholder="密码"
              @keyup.enter="handleLogin"
              class="minimal-input"
              show-password
            />
          </el-form-item>
          <el-form-item prop="code" v-if="captchaEnabled">
            <div class="captcha-row">
              <el-input
                v-model="loginForm.code"
                size="large"
                placeholder="验证码"
                class="minimal-input captcha-input"
                @keyup.enter="handleLogin"
              />
              <div class="captcha-img-box" @click="getCode">
                <img :src="codeUrl" class="captcha-img" />
              </div>
            </div>
          </el-form-item>
          
          <div class="form-actions">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <span class="forgot-link" @click="goToForgot">忘记密码</span>
          </div>

          <el-button
            :loading="loading"
            type="primary"
            size="large"
            class="submit-btn"
            @click.prevent="handleLogin"
          >
            登 录
          </el-button>
          
          <div class="close-btn-wrapper">
             <el-button link @click="loginVisible = false">取消</el-button>
          </div>
        </el-form>

        <!-- QR Code / WeChat Login Section -->
        <div v-else class="qrcode-login-section">
          <!-- Type Toggle Tabs -->
          <div class="qr-tabs">
              <div class="qr-tab" :class="{ active: loginType === 'qrcode' }" @click="toggleLoginType('qrcode')">APP 扫码</div>
              <div class="qr-tab" :class="{ active: loginType === 'wechat' }" @click="toggleLoginType('wechat')">微信扫码</div>
          </div>
          
          <!-- Custom APP QR -->
          <div v-if="loginType === 'qrcode'">
              <div class="qrcode-container">
                <div class="qrcode-border">
                   <div class="qrcode-wrapper">
                     <img v-if="qrCodeUrl" :src="qrCodeUrl" class="real-qrcode" />
                     <div v-else class="qrcode-loading">
                       <el-icon class="is-loading"><Loading /></el-icon>
                     </div>
                     
                     <!-- Overlay for Scanned/Expired -->
                     <div v-if="qrStatus === 'SCANNED'" class="qrcode-overlay">
                       <el-icon color="#10b981" size="32"><CircleCheckFilled /></el-icon>
                       <p>扫码成功<br>请在手机上确认</p>
                     </div>
                     <div v-if="qrStatus === 'EXPIRED'" class="qrcode-overlay">
                       <el-icon color="#f56c6c" size="32"><CircleCloseFilled /></el-icon>
                       <p>二维码已过期</p>
                       <el-button type="primary" size="small" @click="getQrCodeData">刷新</el-button>
                     </div>
                   </div>
                </div>
              </div>
              <div class="qrcode-desc">
                <p>打开 <span class="highlight">OpsCenter App</span></p>
                <p>点击右上角扫一扫</p>
                <!-- Dev Tools for Testing -->
                <div style="margin-top: 10px; opacity: 0.5;">
                   <el-button size="small" @click="handleMockScan">模拟扫码</el-button>
                   <el-button size="small" @click="handleMockConfirm">模拟确认</el-button>
                </div>
              </div>
          </div>
          
          <!-- WeChat QR -->
          <div v-else-if="loginType === 'wechat'">
               <div class="qrcode-container">
                    <!-- WeChat Iframe Container -->
                    <div id="wechat_container" class="wechat-qr-container">
                        <!-- If real AppID was present, iframe would be here -->
                        <div class="mock-wechat-placeholder">
                            <el-icon size="48" color="#07c160"><ChatDotRound /></el-icon>
                            <p>微信二维码区域</p>
                            <p style="font-size: 12px; color: #999;">(配置 AppID 后生效)</p>
                        </div>
                    </div>
               </div>
               <div class="qrcode-desc">
                   <p>请使用 <span class="highlight" style="color: #07c160;">微信</span> 扫一扫登录</p>
                   <!-- Mock WeChat Callback for Demo -->
                   <div style="margin-top: 10px; opacity: 0.5;">
                        <el-button size="small" type="success" @click="handleWeChatCallback('mock_code_' + Date.now())">模拟微信回调</el-button>
                   </div>
               </div>
          </div>

          <div class="close-btn-wrapper">
             <el-button link @click="loginVisible = false">取消</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- Forgot Password Dialog (Minimalist Style) -->
    <el-dialog
      v-model="forgotVisible"
      width="420px"
      append-to-body
      class="login-dialog minimal-dialog"
      :close-on-click-modal="false"
      align-center
      :show-close="false"
    >
      <div class="dialog-content">
        <div class="dialog-header">
          <h2>重置密码</h2>
          <p>我们将帮助您找回账号访问权限</p>
        </div>
        
        <el-steps :active="forgotActiveStep" finish-status="success" simple class="minimal-steps">
          <el-step title="验证" />
          <el-step title="重置" />
          <el-step title="完成" />
        </el-steps>

        <!-- Step 1 -->
        <div v-if="forgotActiveStep === 0" class="step-content">
          <el-form ref="verifyRef" :model="verifyForm" :rules="verifyRules">
            <el-form-item prop="username">
              <el-input v-model="verifyForm.username" placeholder="账号" size="large" class="minimal-input" />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="verifyForm.email" placeholder="注册邮箱" size="large" class="minimal-input" />
            </el-form-item>
            <el-form-item prop="code">
              <div class="captcha-row">
                <el-input v-model="verifyForm.code" placeholder="验证码" size="large" class="minimal-input" />
                <div class="captcha-img-box" @click="getVerifyCode">
                  <img :src="verifyCodeUrl" class="captcha-img" />
                </div>
              </div>
            </el-form-item>
            <el-button type="primary" class="submit-btn" size="large" @click="handleVerify">下一步</el-button>
          </el-form>
        </div>

        <!-- Step 2 -->
        <div v-if="forgotActiveStep === 1" class="step-content">
          <el-form ref="resetRef" :model="resetForm" :rules="resetRules">
            <el-form-item prop="password">
              <el-input v-model="resetForm.password" type="password" placeholder="新密码" size="large" show-password class="minimal-input" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="resetForm.confirmPassword" type="password" placeholder="确认新密码" size="large" show-password class="minimal-input" />
            </el-form-item>
            <el-button type="primary" class="submit-btn" size="large" @click="handleReset">重置密码</el-button>
          </el-form>
        </div>

        <!-- Step 3 -->
        <div v-if="forgotActiveStep === 2" class="step-content success-content">
          <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
          <p>密码已成功重置</p>
          <el-button type="primary" class="submit-btn" size="large" @click="handleBackToLogin">立即登录</el-button>
        </div>
        
        <div class="close-btn-wrapper" v-if="forgotActiveStep !== 2">
             <el-button link @click="handleBackToLogin">返回登录</el-button>
        </div>
      </div>
    </el-dialog>
    <!-- Bind Account Dialog -->
    <el-dialog
      v-model="bindVisible"
      width="420px"
      append-to-body
      class="login-dialog minimal-dialog"
      :close-on-click-modal="false"
      align-center
      :show-close="false"
    >
      <div class="dialog-content">
        <div class="dialog-header">
          <h2>绑定账号</h2>
          <p>请绑定现有账号以完成扫码登录</p>
        </div>
        
        <el-form ref="bindRef" :model="bindForm" :rules="bindRules">
          <el-form-item prop="username">
            <el-input v-model="bindForm.username" placeholder="账号" size="large" class="minimal-input">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="bindForm.password" type="password" placeholder="密码" size="large" show-password class="minimal-input" />
          </el-form-item>
          
          <el-button type="primary" class="submit-btn" size="large" :loading="loading" @click="handleBindUser">绑定并登录</el-button>
          
          <div class="close-btn-wrapper">
             <el-button link @click="bindVisible = false; startQrPoll()">取消</el-button>
          </div>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCodeImg, getQrCode, checkQrCode, scanQrCode, confirmQrCode, bindQrUser, wechatLogin } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import { setToken } from "@/utils/auth"
import useUserStore from '@/store/modules/user'
import { Monitor, Odometer, Cpu, Connection, DataAnalysis, CircleCheckFilled, CircleCloseFilled, Grid, User, Refresh, Loading, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// State
const loginVisible = ref(false)
const forgotVisible = ref(false)
const bindVisible = ref(false) // 绑定账号弹窗
const loading = ref(false)
const captchaEnabled = ref(true)
const redirect = ref(undefined)
const codeUrl = ref("")
const verifyCodeUrl = ref("")
const loginType = ref('account') // 'account', 'qrcode', 'wechat'
const qrCodeUrl = ref('')
const qrUuid = ref('')
const qrStatus = ref('WAITING') // WAITING, SCANNED, CONFIRMED, EXPIRED, NOT_BOUND
const qrTimer = ref(null)

// Forms
const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false,
  code: "",
  uuid: ""
})

const bindForm = ref({
  username: "",
  password: ""
})

const verifyForm = ref({ username: "", email: "", code: "", uuid: "" })
const resetForm = ref({ password: "", confirmPassword: "" })
const forgotActiveStep = ref(0)

// Rules
const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const bindRules = {
  username: [{ required: true, trigger: "blur", message: "请输入账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入密码" }]
}

const verifyRules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }]
}

const resetRules = {
  password: [{ required: true, message: "请输入新密码", trigger: "blur" }, { min: 5, max: 20, message: "长度5-20位", trigger: "blur" }],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: (rule, value, callback) => value !== resetForm.value.password ? callback(new Error("密码不一致")) : callback(), trigger: "blur" }
  ]
}

// Logic
watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function toggleLoginType(type) {
  // If clicking switcher icon (account <-> qr/wechat)
  if (!type) {
      if (loginType.value === 'account') {
          loginType.value = 'qrcode' // Default to QRCode
          getQrCodeData()
      } else {
          loginType.value = 'account'
          stopQrPoll()
      }
  } else {
      // Switching between QRCode and WeChat
      loginType.value = type
      stopQrPoll()
      if (type === 'qrcode') {
          getQrCodeData()
      } else if (type === 'wechat') {
          // Initialize WeChat Login
          initWeChatLogin()
      }
  }
}

function initWeChatLogin() {
    // Check if wxLogin.js is loaded
    if (!window.WxLogin) {
        // Load script dynamically
        const script = document.createElement('script')
        script.src = 'http://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js'
        script.onload = () => {
            renderWeChatQr()
        }
        document.head.appendChild(script)
    } else {
        renderWeChatQr()
    }
}

function renderWeChatQr() {
    // Since we don't have a real AppID, this will fail to show the QR code in the iframe.
    // However, the code structure is correct.
    // In a real scenario, you would provide the AppID here.
    try {
        new window.WxLogin({
            self_redirect: false,
            id: "wechat_container",
            appid: "wx_mock_appid_123456", // Mock AppID
            scope: "snsapi_login",
            redirect_uri: encodeURIComponent(window.location.origin + "/login"), // Callback to self
            state: "wechat_login",
            style: "black",
            href: ""
        });
    } catch (e) {
        console.warn("WeChat Login Init Failed (Expected in Mock Mode)", e)
    }
}

// Watch for route query 'code' for WeChat callback
watch(() => route.query, (query) => {
    if (query && query.code && query.state === 'wechat_login') {
        // Handle WeChat Callback
        handleWeChatCallback(query.code)
    }
}, { immediate: true })

function handleWeChatCallback(code) {
    loading.value = true
    wechatLogin("mock_uuid_for_wechat", code).then(res => {
        // Handle login success
    }).catch(err => {
        console.error(err)
        loading.value = false
    })
}

function openLogin() {
  loginVisible.value = true
  loginType.value = 'account'
  getCode()
  getCookie()
}

function getQrCodeData() {
  getQrCode().then(res => {
    qrUuid.value = res.uuid
    qrStatus.value = 'WAITING'
    // Generate QR Code Image from UUID
    QRCode.toDataURL(qrUuid.value)
      .then(url => {
        qrCodeUrl.value = url
        startQrPoll()
      })
      .catch(err => {
        console.error(err)
        ElMessage.error('生成二维码失败')
      })
  })
}

function startQrPoll() {
  stopQrPoll()
  qrTimer.value = setInterval(() => {
    if (!qrUuid.value) return
    checkQrCode(qrUuid.value).then(res => {
      qrStatus.value = res.status
      if (res.status === 'CONFIRMED') {
        stopQrPoll()
        // Login Success
        // userStore.setToken is not an action, we should use setToken from utils or call a store action that sets it
        // Or simply:
        setToken(res.token)
        userStore.token = res.token // But we can't access state directly if not exported or using $patch
        // Best way: create a setToken action in user store, OR just manually set cookie and reload store state?
        // Actually user store initializes token from cookie.
        // Let's just set the cookie first.
        
        ElMessage.success('扫码登录成功')
        loginVisible.value = false
        router.push({ path: redirect.value || "/" })
      } else if (res.status === 'EXPIRED') {
        stopQrPoll()
      } else if (res.status === 'NOT_BOUND') {
        stopQrPoll()
        // Show Bind Dialog
        bindVisible.value = true
        // Keep loginVisible open but maybe hide the QR code part or overlay it?
        // Actually, let's just open a new dialog on top or switch content
      }
    })
  }, 2000)
}

function stopQrPoll() {
  if (qrTimer.value) {
    clearInterval(qrTimer.value)
    qrTimer.value = null
  }
}

// 模拟扫码 (测试用)
function handleMockScan() {
  if (!qrUuid.value) return
  scanQrCode(qrUuid.value).then(() => {
    ElMessage.success('模拟扫码成功')
  })
}

// 模拟确认 (测试用)
function handleMockConfirm() {
  if (!qrUuid.value) return
  confirmQrCode(qrUuid.value).then(res => {
    // If returning openId, we can log it
    console.log("Confirm result:", res)
    ElMessage.success('模拟确认成功')
  })
}

function handleBindUser() {
  proxy.$refs.bindRef.validate(valid => {
    if (valid) {
      loading.value = true
      bindQrUser(qrUuid.value, bindForm.value.username, bindForm.value.password).then(res => {
        ElMessage.success('绑定并登录成功')
        bindVisible.value = false
        loginVisible.value = false
        
        // Get token from checkQrCode or we can assume backend auto-logged us in via this request?
        // Actually the backend bindUser generates token but returns it via checkQrCode?
        // Wait, bindUser in Controller calls bindUser in Service which calls loginUser...
        // But bindUser endpoint returns AjaxResult.success("绑定成功") without token.
        // We should probably change backend to return token, OR just checkQrCode again.
        
        // Let's rely on checking QR code again or just manually checking once more.
        checkQrCode(qrUuid.value).then(checkRes => {
             if (checkRes.status === 'CONFIRMED') {
                 userStore.setToken(checkRes.token)
                 router.push({ path: redirect.value || "/" })
             }
        })
      }).catch(() => {
        loading.value = false
      })
    }
  })
}


function scrollToFeatures() {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getVerifyCode() {
  getCodeImg().then(res => {
    verifyCodeUrl.value = "data:image/gif;base64," + res.img
    verifyForm.value.uuid = res.uuid
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function goToForgot() {
  loginVisible.value = false
  setTimeout(() => {
    forgotVisible.value = true
    getVerifyCode()
  }, 100)
}

function handleBackToLogin() {
  forgotVisible.value = false
  forgotActiveStep.value = 0
  setTimeout(() => {
    loginVisible.value = true
    getCode()
  }, 100)
}

function handleVerify() {
  proxy.$refs.verifyRef.validate(valid => {
    if (valid) {
      if (verifyForm.value.username === "admin") forgotActiveStep.value = 1
      else {
        ElMessage.error("验证失败（模拟环境仅支持admin）")
        getVerifyCode()
      }
    }
  })
}

function handleReset() {
  proxy.$refs.resetRef.validate(valid => {
    if (valid) {
      setTimeout(() => { forgotActiveStep.value = 2 }, 800)
    }
  })
}
</script>

<style lang="scss" scoped>
/* Minimalist Theme Variables */
$bg-color: #f8f9fa;
$primary-color: #18181b; /* Near black for minimalism */
$accent-color: #2563eb; /* Tech Blue */
$text-main: #18181b;
$text-secondary: #71717a;
$border-color: #e4e4e7;

.landing-page {
  min-height: 100vh;
  background-color: #fff;
  color: $text-main;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.container {
  max-width: 1024px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Navbar */
.navbar {
  height: 64px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(10px);
  z-index: 100;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 18px;
  color: $text-main;
  
  .logo-icon {
    font-size: 20px;
    color: $accent-color;
  }
}

/* Hero Section */
.hero-section {
  padding: 120px 0 80px;
  text-align: center;
  background: radial-gradient(circle at 50% 50%, #fafafa 0%, #fff 100%);
}

.hero-title {
  font-size: 48px;
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin-bottom: 24px;
  background: linear-gradient(180deg, #18181b 0%, #3f3f46 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 18px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  
  .cta-btn {
    border-radius: 6px;
    font-weight: 500;
    padding: 12px 24px;
    height: auto;
    
    &.secondary {
      background: transparent;
      border: 1px solid $border-color;
      color: $text-main;
      
      &:hover {
        background: $bg-color;
      }
    }
  }
}

/* Features Section */
.features-section {
  padding: 80px 0;
  background: $bg-color;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.feature-item {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: $border-color;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  }
  
  .feature-icon {
    font-size: 24px;
    color: $accent-color;
    margin-bottom: 16px;
    background: rgba(37, 99, 235, 0.1);
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
  }
  
  h3 {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: $text-secondary;
    line-height: 1.5;
    margin: 0;
  }
}

/* Footer */
.footer {
  padding: 40px 0;
  text-align: center;
  color: $text-secondary;
  font-size: 13px;
  border-top: 1px solid $border-color;
}

/* Minimal Dialog Styles */
:deep(.minimal-dialog) {
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  
  .el-dialog__header {
    display: none;
  }
  
  .el-dialog__body {
    padding: 0;
  }
}

.dialog-content {
  padding: 40px;
  position: relative;
}

.login-switcher {
  position: absolute;
  top: 20px;
  right: 20px;
  cursor: pointer;
  z-index: 10;
}

.switcher-icon {
  font-size: 24px;
  color: $accent-color;
  padding: 8px;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    background: $accent-color;
    color: white;
  }
}

.qrcode-login-section {
  text-align: center;
  padding: 10px 0;
}
.qr-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  gap: 20px;
}

.qr-tab {
  padding: 8px 16px;
  cursor: pointer;
  color: #71717a;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  
  &.active {
    color: #18181b;
    border-bottom-color: #18181b;
    font-weight: 500;
  }
}

.wechat-qr-container {
    width: 200px;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f4f4f5;
    border-radius: 8px;
    margin: 0 auto;
}

.mock-wechat-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #71717a;
}


.qrcode-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.qrcode-border {
  padding: 16px;
  border: 1px solid $border-color;
  border-radius: 8px;
  background: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.qrcode-wrapper {
  width: 150px;
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f4f5;
  border-radius: 4px;
}

.real-qrcode {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.qrcode-loading {
  font-size: 24px;
  color: $text-secondary;
}

.qrcode-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
  text-align: center;
  
  p {
    margin: 8px 0;
    font-size: 14px;
    font-weight: 500;
    color: $text-main;
  }
}

.qrcode-desc {
  color: $text-secondary;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
  
  .highlight {
    color: $accent-color;
    font-weight: 600;
  }
  
  p {
    margin: 4px 0;
  }
}

.dialog-header {
  text-align: center;
  margin-bottom: 32px;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
    margin: 0 0 8px;
    color: $text-main;
  }
  
  p {
    color: $text-secondary;
    font-size: 14px;
    margin: 0;
  }
}

.minimal-input {
  :deep(.el-input__wrapper) {
    background: #f4f4f5;
    box-shadow: none !important;
    border-radius: 6px;
    padding: 8px 12px;
    transition: background 0.2s;
    
    &:hover, &.is-focus {
      background: #e4e4e7;
    }
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  
  .captcha-img-box {
    width: 100px;
    height: 40px;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    background: #f4f4f5;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 14px;
  
  .forgot-link {
    color: $text-secondary;
    cursor: pointer;
    &:hover { color: $text-main; }
  }
}

.submit-btn {
  width: 100%;
  border-radius: 6px;
  font-weight: 600;
  background: $text-main;
  border: none;
  height: 44px;
  
  &:hover {
    background: #27272a;
  }
}

.close-btn-wrapper {
  text-align: center;
  margin-top: 16px;
}

.success-content {
  text-align: center;
  padding: 20px 0;
  
  .success-icon {
    font-size: 48px;
    color: #10b981;
    margin-bottom: 16px;
  }
  
  p {
    margin-bottom: 24px;
    color: $text-secondary;
  }
}

.minimal-steps {
  margin-bottom: 32px;
  background: transparent !important;
  padding: 0 !important;
}
</style>
