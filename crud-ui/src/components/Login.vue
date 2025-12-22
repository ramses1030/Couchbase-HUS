<script setup>
import { ref, defineEmits } from 'vue'
import authService from '../services/authService'
import logo from '../assets/images/hus.png'
import successGif from '../assets/success.svg'
import errorGif from '../assets/error.svg'

const emit = defineEmits(['login-success'])

const LAST_LOGIN_KEY = 'crud_ui_last_login'

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const logoLoaded = ref(true)
const onLogoError = () => { logoLoaded.value = false }

// Estados para animaciones de resultado
const showSuccess = ref(false)
const showError = ref(false)
const prevLoginDisplay = ref(null)
const RESULT_SHOW_MS = 1200

const submit = async () => {
  error.value = ''
  loading.value = true
  showError.value = false

  // Capturar el último inicio (si existe) antes de sobrescribir
  const previousISO = localStorage.getItem(LAST_LOGIN_KEY)
  prevLoginDisplay.value = previousISO ? new Date(previousISO).toLocaleString() : null

  try {
    const token = await authService.login(username.value, password.value)

    // Guardar la marca de tiempo del login actual
    const now = new Date().toISOString()
    localStorage.setItem(LAST_LOGIN_KEY, now)

    // Mostrar animación de éxito que incluye el último inicio
    showSuccess.value = true
    await new Promise(r => setTimeout(r, RESULT_SHOW_MS))

    // Emitimos evento al componente padre con success y previousLogin (ISO)
    emit('login-success', { token, previousLogin: previousISO })
  } catch (e) {
    error.value = e?.message || 'Error autenticando'
    // Mostrar animación de error
    showError.value = true
    await new Promise(r => setTimeout(r, RESULT_SHOW_MS))
    showError.value = false
  } finally {
    showSuccess.value = false
    loading.value = false
  }
}
</script>

<template>
  <div class="d-flex align-items-center justify-content-center min-vh-100">
    <div class="card p-4 position-relative" style="min-width:320px; max-width:420px; width:100%">
      <div class="card-body">
        <!-- Overlay de resultado -->
        <div v-if="showSuccess || showError" class="animation-overlay">
          <img :src="showSuccess ? successGif : errorGif" alt="resultado" />
          <div class="anim-text">{{ showSuccess ? 'Usuario correcto' : 'Usuario/contraseña inválidos' }}</div>
          <div v-if="showSuccess" class="anim-subtext">{{ prevLoginDisplay ? ('Último inicio: ' + prevLoginDisplay) : 'Primer inicio de sesión' }}</div>
        </div>

        <div class="text-center mb-3">
          <img v-if="logoLoaded" :src="logo" alt="Logo" class="login-logo" @error="onLogoError" />
          <div v-else class="login-logo placeholder">Logo</div>
        </div>

        <h5 class="card-title mb-3">Iniciar sesión</h5>

        <div v-if="error" class="alert alert-danger small">{{ error }}</div>

        <div class="mb-3">
          <label class="form-label">Usuario</label>
          <input class="form-control" v-model="username" :disabled="loading || showSuccess || showError" />
        </div>

        <div class="mb-3">
          <label class="form-label">Contraseña</label>
          <input type="password" class="form-control" v-model="password" :disabled="loading || showSuccess || showError" />
        </div>

        <div class="d-flex justify-content-between align-items-center">
          <button class="btn btn-primary" @click="submit" :disabled="loading || showSuccess || showError || !username || !password">
            <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            Entrar
          </button>
          <small class="text-muted">demo: <strong>admin</strong>/<strong>admin</strong></small>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.login-logo {
  height: 72px;
  max-width: 160px;
  object-fit: contain;
  border-radius: 6px;
}
.login-logo.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 72px;
  width: 160px;
  background: #f1f1f1;
  color: #333;
  border-radius: 6px;
  font-weight: 600;
}
.anim-text { margin-top: 10px; font-weight: 700; color: #fff; }

.animation-overlay img { width: 120px; height: auto; }
.animation-overlay { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,0.54); z-index: 1100; border-radius: 6px; flex-direction: column; }
.anim-subtext { margin-top: 6px; color: rgba(255,255,255,0.95); font-size: 0.9rem; }

</style>
