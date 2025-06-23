<template>
    <h3>首頁 {{ userStore.email }}</h3>
    <div class="address-container">
        <input
            v-model="address"
            type="text"
            placeholder="輸入台灣地址（例如：台灣台北市中正區忠孝東路一段100號）"
            class="address-input"
        >
        <button @click="getCoordinates" class="query-button">查詢座標</button>
        <p v-if="loading" class="loading">正在查詢...</p>
        <p v-else-if="coordinates" class="result">
            經緯度：{{ coordinates.lat }}, {{ coordinates.lon }}
        </p>
        <p v-else-if="error" class="error">{{ error }}</p>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import useUserStore from "@/stores/user.js";

const userStore = useUserStore();
const address = ref(''); // 儲存輸入的地址
const coordinates = ref(null); // 儲存查詢到的座標
const loading = ref(false); // 控制載入狀態
const error = ref(''); // 儲存錯誤訊息

// 格式化地址的函數
const formatAddress = (input) => {
    if (!input.trim()) return input;

    // 完整地址的正則表達式
    const fullAddressRegex = /^(\S+?)([縣市])(.+?)(區|鄉|鎮|市)(.*?)(\d+號)/;
    // 簡化地址的正則表達式（僅路名和號）
    const simpleAddressRegex = /^(.+?)(\d+號)$/;

    let match = input.match(fullAddressRegex);
    if (match) {
        // 提取完整地址的各部分
        const country = match[1]; // 國名 (如台灣)
        const city = match[2]; // 縣市 (如台北市)
        const district = match[3] + match[4]; // 行政區 (如信義區)
        const road = match[5].trim(); // 路段巷弄 (如松山路465巷27弄)
        const number = match[6]; // 號 (如16號)

        // 組合格式化後的地址
        return `${country} ${city} ${district} ${road} ${number}`;
    }

    // 檢查是否為簡化地址
    match = input.match(simpleAddressRegex);
    if (match) {
        // 提取簡化地址的各部分
        const road = match[1].trim(); // 路名 (如松江路)
        const number = match[2]; // 號 (如146號)

        // 組合格式化後的地址
        return `${road} ${number}`;
    }

    // 如果無法匹配，保留原始輸入
    return input;
};

// 查詢 Nominatim API 的函數
const getCoordinates = async () => {
    if (!address.value.trim()) {
        error.value = '請輸入地址';
        coordinates.value = null;
        return;
    }

    // 格式化地址
    address.value = formatAddress(address.value);

    loading.value = true;
    error.value = '';
    coordinates.value = null;

    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address.value)}`;
        const response = await fetch(url, {
            headers: {
                'User-Agent': 'Jimmy/tokin81@yahoo.com.tw' // 請替換為你的應用名稱和聯繫方式
            }
        });
        const data = await response.json();

        if (data.length > 0) {
            coordinates.value = {
                lat: data[0].lat,
                lon: data[0].lon
            };
        } else {
            error.value = '無法找到該地址的座標';
        }
    } catch (err) {
        error.value = '查詢失敗，請稍後再試';
        console.error('API 錯誤:', err);
    } finally {
        loading.value = false;
    }
};
</script>

<style scoped>
.address-container {
    margin-top: 20px;
    max-width: 600px;
}

.address-input {
    width: 100%;
    padding: 10px;
    font-size: 16px;
    border: 1px solid #ccc;
    border-radius: 4px;
    margin-bottom: 10px;
}

.query-button {
    padding: 10px 20px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.query-button:hover {
    background-color: #0056b3;
}

.loading {
    color: #888;
    margin-top: 10px;
}

.result {
    color: #28a745;
    font-weight: bold;
    margin-top: 10px;
}

.error {
    color: #dc3545;
    margin-top: 10px;
}
</style>