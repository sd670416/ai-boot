<template>
  <div class="login-shell">
    <div class="login-panel">
      <div class="brand-block">
        <span class="eyebrow">Vue 3 + Naive UI</span>
        <h1>AI Boot 电商管理系统</h1>
        <p>前后台用户已经拆表，登录入口也拆分为后台登录和前台登录，后台走角色控制，前台承接商城业务。</p>
      </div>
      <n-card :bordered="false" style="border-radius: 24px; box-shadow: 0 20px 50px rgba(20,83,45,0.12)">
        <n-tabs type="segment" animated>
          <n-tab-pane name="admin-login" tab="后台登录">
            <n-form label-placement="top">
              <n-form-item label="用户名">
                <n-input v-model:value="adminLoginForm.username" placeholder="admin 或 operator01" />
              </n-form-item>
              <n-form-item label="密码">
                <n-input v-model:value="adminLoginForm.password" type="password" show-password-on="click" />
              </n-form-item>
              <n-button type="primary" block size="large" @click="handleAdminLogin">登录后台</n-button>
            </n-form>
          </n-tab-pane>
          <n-tab-pane name="frontend-login" tab="前台登录">
            <n-form label-placement="top">
              <n-form-item label="用户名">
                <n-input v-model:value="frontendLoginForm.username" placeholder="buyer01" />
              </n-form-item>
              <n-form-item label="密码">
                <n-input v-model:value="frontendLoginForm.password" type="password" show-password-on="click" />
              </n-form-item>
              <n-button type="primary" block size="large" @click="handleFrontendLogin">登录前台</n-button>
            </n-form>
          </n-tab-pane>
          <n-tab-pane name="register" tab="前台注册">
            <n-form label-placement="top">
              <div class="form-grid">
                <n-form-item label="用户名">
                  <n-input v-model:value="registerForm.username" />
                </n-form-item>
                <n-form-item label="昵称">
                  <n-input v-model:value="registerForm.nickname" />
                </n-form-item>
                <n-form-item label="密码">
                  <n-input v-model:value="registerForm.password" type="password" show-password-on="click" />
                </n-form-item>
                <n-form-item label="手机号">
                  <n-input v-model:value="registerForm.phone" />
                </n-form-item>
                <n-form-item label="邮箱">
                  <n-input v-model:value="registerForm.email" />
                </n-form-item>
              </div>
              <n-button type="primary" block size="large" @click="handleRegister">注册前台账号</n-button>
            </n-form>
          </n-tab-pane>
        </n-tabs>
        <n-alert type="info" style="margin-top:16px;">
          后台账号：admin / 123456；运营账号：operator01 / 123456；前台账号：buyer01 / 123456
        </n-alert>
      </n-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NAlert, NButton, NCard, NForm, NFormItem, NInput, NTabPane, NTabs, useMessage } from 'naive-ui'
import { api } from '@/api'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const message = useMessage()
const auth = useAuthStore()

const adminLoginForm = reactive({ username: 'admin', password: '123456' })
const frontendLoginForm = reactive({ username: 'buyer01', password: '123456' })
const registerForm = reactive({ username: '', password: '', nickname: '', phone: '', email: '' })

async function handleAdminLogin() {
  try {
    await auth.login('admin', adminLoginForm)
    message.success('后台登录成功')
    router.replace('/dashboard')
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function handleFrontendLogin() {
  try {
    await auth.login('frontend', frontendLoginForm)
    message.success('前台登录成功')
    router.replace('/products')
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function handleRegister() {
  try {
    await api.frontendRegister(registerForm)
    message.success('注册成功，请使用前台登录')
    registerForm.username = ''
    registerForm.password = ''
    registerForm.nickname = ''
    registerForm.phone = ''
    registerForm.email = ''
  } catch (error) {
    message.error((error as Error).message)
  }
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(132, 204, 22, 0.25), transparent 28%),
    radial-gradient(circle at bottom right, rgba(20, 83, 45, 0.2), transparent 32%),
    linear-gradient(135deg, #f5f5f4 0%, #dcfce7 100%);
}

.login-panel {
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 28px;
  align-items: center;
}

.brand-block h1 {
  margin: 12px 0;
  font-size: clamp(36px, 6vw, 58px);
  line-height: 1.05;
  color: #14532d;
}

.brand-block p {
  max-width: 520px;
  font-size: 18px;
  color: #44403c;
  line-height: 1.7;
}

.eyebrow {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(20, 83, 45, 0.1);
  color: #166534;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

@media (max-width: 900px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
}
</style>
