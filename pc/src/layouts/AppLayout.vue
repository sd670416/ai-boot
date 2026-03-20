<template>
  <n-layout has-sider class="app-shell">
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="72" :width="240" show-trigger>
      <div class="brand">AI Boot</div>
      <n-menu :value="activeKey" :options="menuOptions" @update:value="handleMenuChange" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="app-header">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p>{{ auth.user?.nickname || auth.user?.username }}</p>
        </div>
        <n-space align="center">
          <n-tag type="success" size="large">{{ auth.user?.userType || 'GUEST' }}<template v-if="auth.isAdmin && auth.user?.roleCode"> / {{ auth.user.roleCode }}</template></n-tag>
          <n-button tertiary @click="handleLogout">退出登录</n-button>
        </n-space>
      </n-layout-header>
      <n-layout-content content-style="padding: 24px;">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NLayout, NLayoutContent, NLayoutHeader, NLayoutSider, NMenu, NSpace, NTag } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menuOptions = computed<MenuOption[]>(() => {
  if (auth.isAdmin) {
    return [
      { label: '数据总览', key: '/dashboard' },
      { label: '参数配置', key: '/system-params' },
      { label: '后台用户管理', key: '/users/backend' },
      { label: '前台用户管理', key: '/users/frontend' },
      { label: '商品管理', key: '/products' },
      { label: '订单管理', key: '/orders' },
    ]
  }
  return [
    { label: '商品中心', key: '/products' },
    { label: '购物车', key: '/cart' },
    { label: '地址管理', key: '/addresses' },
    { label: '我的订单', key: '/orders' },
  ]
})

const activeKey = computed(() => route.path)

const pageTitle = computed(() => {
  switch (route.path) {
    case '/dashboard':
      return '数据总览'
    case '/system-params':
      return '参数配置'
    case '/users/backend':
      return '后台用户管理'
    case '/users/frontend':
      return '前台用户管理'
    case '/products':
      return auth.isAdmin ? '商品管理' : '商品中心'
    case '/orders':
      return auth.isAdmin ? '订单管理' : '我的订单'
    case '/cart':
      return '购物车'
    case '/addresses':
      return '地址管理'
    default:
      return 'AI Boot'
  }
})

function handleMenuChange(key: string) {
  router.push(key)
}

function handleLogout() {
  auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
}

.brand {
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  font-size: 22px;
  font-weight: 700;
  color: #14532d;
  letter-spacing: 0.08em;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
}

.app-header h1 {
  margin: 0;
  font-size: 26px;
  color: #14532d;
}

.app-header p {
  margin: 6px 0 0;
  color: #64748b;
}
</style>
