import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    extensions: ['.js', '.jsx'], // explicitly resolve jsx
    alias: {
      '@': path.resolve(__dirname, 'src'), // optional: use @/ for imports
    },
  },
  build: {
    outDir: 'dist',  // default Vite dist folder
  },
})
