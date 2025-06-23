<template>
    <div>
        <h2>輸入文字</h2>
        <textarea v-model="content" placeholder="請輸入文字"></textarea>
        <button @click="submitText">提交</button>
        <h2>已儲存的文字</h2>
        <ul>
            <li v-for="text in texts" :key="text.id">
                {{ text.content }} ({{ text.createdAt }})
            </li>
        </ul>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axiosapi from "@/plugins/axios.js"

const content = ref('');
const texts = ref([]);

const submitText = async () => {
    if (!content.value.trim()) {
        alert('請輸入文字');
        return;
    }
    try {
        const response = await axiosapi.post('/api/text', { content: content.value });
        texts.value.unshift(response.data); // 新增到列表頂部
        content.value = ''; // 清空輸入框
    } catch (error) {
        console.error('提交失敗:', error);
        alert('提交失敗');
    }
};

const fetchTexts = async () => {
    try {
        const response = await axiosapi.get('/api/text');
        texts.value = response.data;
    } catch (error) {
        console.error('獲取失敗:', error);
    }
};

onMounted(fetchTexts);
</script>

<style scoped>
textarea {
    width: 100%;
    height: 100px;
    margin-bottom: 10px;
}

button {
    padding: 5px 10px;
}

ul {
    list-style: none;
    padding: 0;
}

li {
    padding: 5px;
    border-bottom: 1px solid #eee;
}
</style>