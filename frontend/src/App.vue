<template>
  <div id="app" :class="{ 'is-mobile': isMobile }">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      isMobile: false
    }
  },
  watch: {
    $route: {
      immediate: true,
      handler(to) {
        this.isMobile = to.meta?.isMobile || /^\/m\//.test(to.path)
      }
    }
  }
}
</script>
