//import { defineConfig } from 'vite'
import { defineConfig } from 'vitest/config'; // needed by test property
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { visualizer } from 'rollup-plugin-visualizer';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), visualizer()],
  test: {
    environment: 'jsdom',
    globals: true,
  },
})
