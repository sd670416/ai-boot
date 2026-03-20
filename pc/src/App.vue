<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-dialog-provider>
      <n-notification-provider>
        <n-message-provider>
          <n-loading-bar-provider>
            <router-view />
          </n-loading-bar-provider>
        </n-message-provider>
      </n-notification-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import {
  NConfigProvider,
  NDialogProvider,
  NLoadingBarProvider,
  NMessageProvider,
  NNotificationProvider,
  type GlobalThemeOverrides,
} from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#14532d',
    primaryColorHover: '#166534',
    primaryColorPressed: '#166534',
    infoColor: '#0f766e',
    successColor: '#15803d',
    warningColor: '#c2410c',
    errorColor: '#b91c1c',
    bodyColor: '#f5f5f4',
    cardColor: '#ffffff',
    textColorBase: '#292524',
  },
  Button: {
    textColorPrimary: '#ffffff',
  },
}
</script>
