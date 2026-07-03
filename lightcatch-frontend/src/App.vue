<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#7c3aed',
        borderRadius: 8,
        colorBgContainer: '#ffffff',
      },
    }"
  >
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f5f5f5;
}
</style>
