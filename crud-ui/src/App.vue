<script setup>
import { ref, onMounted, computed } from 'vue'
import { getItems, getItem, createItem, deleteItem } from './services/itemService'
import logo from './assets/images/hus2.jpg'
import couchbaseLogo from './assets/images/png-transparent-couchbase-hd-logo.png' 

const items = ref([])

const id = ref('')
const name = ref('')
const description = ref('')

const showDeleteModal = ref(false)
const itemToDelete = ref(null)
const errorMsg = ref('')
const creating = ref(false)
const deleting = ref(false)
const reloading = ref(false)
const notification = ref({ show: false, type: 'success', message: '', icon: '' })

const showNotification = (type, message, icon) => {
  notification.value = { show: true, type, message, icon }
  if (type !== 'loading') {
    setTimeout(() => {
      notification.value.show = false
    }, 2500)
  }
}

const hideNotification = () => {
  notification.value.show = false
}

// Estado para detectar si los logos cargaron correctamente
const logoLoaded = ref(true)
const couchLogoLoaded = ref(true)
const idTouched = ref(false)

const isIdValid = computed(() => {
  const raw = id.value?.toString().trim()
  return !!raw && /^\d+$/.test(raw)
})

const sanitizeId = (e) => {
  // elimina cualquier caracter que no sea dígito
  id.value = (e?.target?.value || '').replace(/\D/g, '')
}

const onLogoError = () => {
  logoLoaded.value = false
  showNotification('danger', 'Logo no cargó', 'bi bi-exclamation-circle-fill')
}

const onCouchLogoError = () => {
  couchLogoLoaded.value = false
  showNotification('danger', 'Logo Couchbase no cargó', 'bi bi-exclamation-circle-fill')
} 

const loadItems = async () => {
  reloading.value = true
  try {
    const res = await getItems()
    items.value = res.data
    errorMsg.value = ''
  } catch (e) {
    errorMsg.value = e?.message || 'Error cargando items'
    showNotification('danger', errorMsg.value, 'bi bi-x-circle-fill')
  } finally {
    reloading.value = false
  }
} 

const addItem = async () => {
  // Validación: requerir los 3 campos y que el ID sea numérico entero
  if (!id.value?.toString().trim() || !name.value?.trim() || !description.value?.trim() || !isIdValid.value) {
    showNotification('danger', 'Completa los 3 campos y asegúrate de que el ID sea un número entero', 'bi bi-exclamation-circle-fill')
    idTouched.value = true
    return
  }

  creating.value = true
  errorMsg.value = ''
  showNotification('loading', 'Creando...', '')
  try {
    await createItem({
      id: id.value.trim(),
      name: name.value.trim(),
      description: description.value.trim()
    })
    id.value = ''
    name.value = ''
    description.value = ''
    await loadItems()
    showNotification('success', 'OK', 'bi bi-check-circle-fill')
  } catch (e) {
    errorMsg.value = e?.message || 'Error creando item'
    showNotification('danger', errorMsg.value, 'bi bi-x-circle-fill')
  } finally {
    creating.value = false
  }
}

const askDelete = (item) => {
  itemToDelete.value = item
  showDeleteModal.value = true
}

const cancelDelete = () => {
  itemToDelete.value = null
  showDeleteModal.value = false
}

const confirmDelete = async () => {
  if (!itemToDelete.value) return
  const idToDelete = itemToDelete.value.id

  // Verificar existencia previo al DELETE para evitar 404
  try {
    await getItem(idToDelete)
  } catch (e) {
    const status = e?.response?.status
    if (status === 404) {
      showNotification('danger', `No existe el item ${idToDelete}`, 'bi bi-exclamation-triangle-fill')
      cancelDelete()
      await loadItems()
      return
    }
    const publicMsg = e?.response?.data?.message || e?.response?.data?.error || e?.message || 'Error verificando item'
    showNotification('danger', publicMsg, 'bi bi-x-circle-fill')
    return
  }

  deleting.value = true
  showNotification('loading', 'Eliminando...', '')

  try {
    await deleteItem(idToDelete)
    cancelDelete()
    await loadItems()
    showNotification('success', `Eliminado: ${idToDelete}`, 'bi bi-check-circle-fill')
  } catch (e) {
    // Mejor detalle para depuración
    const status = e?.response?.status
    const url = e?.config?.url || 'unknown URL'
    const data = e?.response?.data
    console.error('Delete failed', { idToDelete, status, url, data, error: e })

    const publicMsg = e?.response?.data?.message || e?.response?.data?.error || e?.message || 'Error eliminando item'
    errorMsg.value = publicMsg
    showNotification('danger', `Request failed (${status || '???'}): ${publicMsg}`, 'bi bi-x-circle-fill')
  } finally {
    deleting.value = false
  }
}

onMounted(loadItems)
</script>

<template>
  <div class="container py-4">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <div class="d-flex align-items-center">
        <img v-if="logoLoaded" :src="logo" alt="Logo" class="app-logo me-2" @error="onLogoError" />
        <div v-else class="app-logo placeholder me-2">Logo</div>
        <h2 class="mb-0">CRUD Items</h2>
      </div> 
      <button class="btn btn-outline-primary" @click="loadItems" :disabled="creating || deleting || reloading">
        <i :class="['bi','bi-arrow-clockwise','me-1',{ rotating: reloading }]" aria-hidden="true"></i> Recargar
      </button> 
    </div>

    <div v-if="errorMsg" class="alert alert-danger">
      {{ errorMsg }}
    </div>

    <!-- Notificación (centrada) -->
    <div v-if="notification.show && notification.type === 'loading'" class="backdrop-center"></div>
    <div v-if="notification.show"
      :class="['notification', notification.type === 'loading' ? 'loading' : (notification.type === 'success' ? 'success' : 'danger')]">
      <div v-if="notification.type === 'loading'" class="spinner-gif" aria-hidden="true"></div>
      <i v-else :class="notification.icon + ' me-2'"></i>
      <div>{{ notification.message }}</div>
      <button v-if="notification.type !== 'loading'" type="button" class="btn-close btn-close-white ms-3"
        @click="hideNotification"></button>
    </div>

    <div class="card mb-4">
      <div class="card-header fw-semibold">Crear</div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label">ID</label>
            <input class="form-control" v-model="id" @input="sanitizeId" @blur="idTouched = true" placeholder="Ej: 50" />
            <div v-if="idTouched && !isIdValid" class="form-text text-danger">El ID debe contener solo dígitos enteros</div>
          </div>
          <div class="col-md-4">
            <label class="form-label">Nombre</label>
            <input class="form-control" v-model="name" placeholder="Nombre" />
          </div>
          <div class="col-md-5">
            <label class="form-label">Descripción</label>
            <input class="form-control" v-model="description" placeholder="Descripción" />
          </div>
        </div>

        <div class="mt-3">
          <button class="btn btn-primary" @click="addItem" :disabled="creating || !id || !name || !description || !isIdValid">
            <span v-if="creating" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            <i v-else class="bi bi-plus-circle me-1"></i>
            Crear
          </button>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header fw-semibold">Lista</div>
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-striped table-hover mb-0 align-middle">
            <thead class="table-light">
              <tr>
                <th style="width: 120px;">ID</th>
                <th style="width: 220px;">Nombre</th>
                <th>Descripción</th>
                <th style="width: 140px;" class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="items.length === 0">
                <td colspan="4" class="text-center py-4 text-muted">Sin items</td>
              </tr>

              <tr v-for="it in items" :key="it.id">
                <td class="fw-semibold">{{ it.id }}</td>
                <td>{{ it.name }}</td>
                <td>{{ it.description }}</td>
                <td class="text-end">
                  <button class="btn btn-outline-danger btn-sm" @click="askDelete(it)">
                    <i class="bi bi-trash"></i> Eliminar
                  </button>

                  <!-- Modal confirmación -->
                  <div v-if="showDeleteModal" class="modal fade show d-block" tabindex="-1">
                    <div class="modal-dialog">
                      <div class="modal-content">
                        <div class="modal-header">
                          <h5 class="modal-title">Confirmar eliminación</h5>
                          <button type="button" class="btn-close" @click="cancelDelete"></button>
                        </div>

                        <div class="modal-body">
                          <p>
                            ¿Seguro que quieres eliminar el item
                            <strong>{{ itemToDelete?.id }}</strong>?
                          </p>
                          <p class="text-muted mb-0">
                            Esta acción no se puede deshacer.
                          </p>
                        </div>

                        <div class="modal-footer">
                          <button class="btn btn-secondary" @click="cancelDelete">Cancelar</button>
                          <button class="btn btn-danger" @click="confirmDelete" :disabled="deleting">
                            <span v-if="deleting" class="spinner-border spinner-border-sm me-2" role="status"
                              aria-hidden="true"></span>
                            Sí, eliminar
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Backdrop -->
                  <div v-if="showDeleteModal" class="modal-backdrop fade show"></div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

      <img v-if="couchLogoLoaded" :src="couchbaseLogo" alt="Couchbase" class="couchbase-logo" @error="onCouchLogoError" />
      <div v-else class="couchbase-logo placeholder">Couchbase</div>
  </div>
</template>

<style scoped>
.notification {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1055;
  padding: 12px 18px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 180px;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.2);
}

.notification.loading {
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
}

.notification.success {
  background: #198754;
  color: #fff;
}

.notification.danger {
  background: #dc3545;
  color: #fff;
}

.spinner-gif {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  box-sizing: border-box;
  border: 4px solid rgba(255, 255, 255, 0.15);
  border-top-color: #fff;
  animation: spin 1s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.rotating {
  animation: spin 0.9s linear infinite;
  transform-origin: 50% 50%;
}

.backdrop-center {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 1050;
}

.app-logo {
  height: 96px; /* tamaño aumentado */
  width: auto;
  object-fit: contain;
  border-radius: 6px;
}

@media (max-width: 576px) {
  .app-logo { height: 56px; }
}

@media (min-width: 1200px) {
  .app-logo { height: 128px; }
}

.couchbase-logo {
  position: fixed;
  right: 16px;
  bottom: 16px;
  width: 80px;
  height: auto;
  opacity: 0.95;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.25));
  transition: transform .18s ease, opacity .18s ease;
  z-index: 1060;
}
.couchbase-logo:hover { transform: translateY(-4px); opacity: 1; }
/* Placeholder visible cuando la imagen no carga */
.app-logo.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f1f1f1;
  color: #333;
  font-weight: 600;
  height: 40px;
  min-width: 40px;
  padding: 0 8px;
  border-radius: 6px;
  border: 1px dashed rgba(0,0,0,0.08);
}

.couchbase-logo.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  color: #333;
  font-weight: 600;
  height: 40px;
  width: 80px;
  border-radius: 6px;
  border: 1px dashed rgba(0,0,0,0.06);
  box-shadow: 0 6px 14px rgba(0,0,0,0.06);
}
</style>
