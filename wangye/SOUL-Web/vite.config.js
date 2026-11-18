import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 5090,
    proxy: {
      '/api': {
        target: 'http://112.126.72.20:8080',
        changeOrigin: true
      }
    }
  }
})