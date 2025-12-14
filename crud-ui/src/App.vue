<script setup>
import { ref, onMounted } from "vue";
import { getItems, createItem, deleteItem } from "./services/itemService";

const items = ref([]);
const form = ref({ id: "", name: "", description: "" });
const error = ref("");
const loading = ref(false);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const res = await getItems();
    items.value = res.data;
  } catch (e) {
    error.value = e?.response?.data?.error || e.message;
  } finally {
    loading.value = false;
  }
}

async function add() {
  error.value = "";
  try {
    // Si no quieres escribir id, lo generamos
    const id = form.value.id?.trim() || Date.now().toString();

    await createItem({
      id,
      name: form.value.name,
      description: form.value.description,
    });

    form.value = { id: "", name: "", description: "" };
    await load();
  } catch (e) {
    error.value = e?.response?.data?.error || e.message;
  }
}

async function remove(id) {
  error.value = "";
  try {
    await deleteItem(id);
    await load();
  } catch (e) {
    error.value = e?.response?.data?.error || e.message;
  }
}

onMounted(load);
</script>

<template>
  <main style="max-width: 900px; margin: 40px auto; font-family: Arial;">
    <h1>CRUD Items (Vue + Spring + Couchbase)</h1>

    <p v-if="error" style="color:#b00020; font-weight:bold;">{{ error }}</p>

    <section style="border:1px solid #ddd; padding:16px; border-radius:8px; margin-bottom:18px;">
      <h2>Crear</h2>

      <div style="display:grid; gap:10px; grid-template-columns:160px 1fr;">
        <label>ID (opcional)</label>
        <input v-model="form.id" placeholder="30" />

        <label>Name</label>
        <input v-model="form.name" placeholder="Item 30" />

        <label>Description</label>
        <input v-model="form.description" placeholder="Texto..." />
      </div>

      <button @click="add" style="margin-top:12px; padding:8px 14px;">
        Crear
      </button>
    </section>

    <section style="border:1px solid #ddd; padding:16px; border-radius:8px;">
      <h2>Lista</h2>

      <button @click="load" style="padding:8px 14px; margin-bottom:10px;">
        Recargar
      </button>

      <p v-if="loading">Cargando...</p>

      <table v-if="!loading" style="width:100%; border-collapse:collapse;">
        <thead>
          <tr>
            <th style="border-bottom:1px solid #ddd; text-align:left; padding:8px;">ID</th>
            <th style="border-bottom:1px solid #ddd; text-align:left; padding:8px;">Name</th>
            <th style="border-bottom:1px solid #ddd; text-align:left; padding:8px;">Description</th>
            <th style="border-bottom:1px solid #ddd; padding:8px;">Acciones</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="it in items" :key="it.id">
            <td style="border-bottom:1px solid #eee; padding:8px;">{{ it.id }}</td>
            <td style="border-bottom:1px solid #eee; padding:8px;">{{ it.name }}</td>
            <td style="border-bottom:1px solid #eee; padding:8px;">{{ it.description }}</td>
            <td style="border-bottom:1px solid #eee; padding:8px; text-align:center;">
              <button @click="remove(it.id)" style="padding:6px 10px;">
                Eliminar
              </button>
            </td>
          </tr>

          <tr v-if="items.length === 0">
            <td colspan="4" style="padding:10px; color:#666;">Sin items</td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>
