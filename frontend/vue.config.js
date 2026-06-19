const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:19465',
        changeOrigin: true
      }
    }
  },
  outputDir: '../src/main/resources/static',
  assetsDir: 'static',
  indexPath: 'index.html'
})
