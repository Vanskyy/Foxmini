import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/modules/auth'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 初始化缓存用户
const auth = useAuthStore()
auth.initFromCache()

app.mount('#app')
