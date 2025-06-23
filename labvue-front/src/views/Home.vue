<template>
    <section class="hero-section">
        <h1>探索附近美食</h1>
        <p>當前位置：{{ address || '台北市中正區' }}</p>
        <input type="text" placeholder="輸入您的地址" v-model="address" />
        <button @click="getCoordinates">搜尋</button>
        <button style="background: transparent; border: none; color: white;" @click="getCurrentLocation">📍</button>
        <p v-if="loading" class="loading">正在查詢...</p>
        <p v-else-if="coordinates" class="result">
            經緯度：{{ coordinates.lat }}, {{ coordinates.lon }}
        </p>
        <p v-else-if="error" class="error">{{ error }}</p>
    </section>

</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

//地址查詢用
const address = ref('請輸入要查詢的地址'); // 儲存輸入的地址
const coordinates = ref(null); // 儲存查詢到的座標
const loading = ref(false); // 控制載入狀態
const error = ref(''); // 儲存錯誤訊息


// 輸入地址查詢
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


// 獲取當前位置
const getCurrentLocation = async () => {
    if (!navigator.geolocation) {
        alert('您的瀏覽器不支援定位功能');
        return;
    }

    try {
        const position = await new Promise((resolve, reject) => {
            navigator.geolocation.getCurrentPosition(resolve, reject);
        });

        const { latitude, longitude } = position.coords;

        // 使用 OpenStreetMap Nominatim API 將座標轉為地址
        const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&addressdetails=1`
        );
        const data = await response.json();

        if (data && data.display_name) {
            address.value = data.display_name; // 更新地址
        } else {
            alert('無法解析地址，請稍後再試');
        }
    } catch (error) {
        console.error('定位失敗:', error);
        alert('無法獲取位置，請檢查權限或稍後再試');
    }
};

</script>

<style scoped>
/* 搜尋與位置區域 */
.hero-section {
    background-color: #fff;
    padding: 20px;
    text-align: center;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin: 20px;
    border-radius: 8px;
}

.hero-section h1 {
    font-size: 30px;
    margin-bottom: 10px;
}

.hero-section p {
    color: #666;
    margin-bottom: 15px;
}

.hero-section input {
    width: 60%;
    max-width: 400px;
    padding: 10px;
    font-size: 16px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.hero-section button {
    padding: 10px 20px;
    background-color: #d70f64;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    margin-left: 10px;
}
</style>