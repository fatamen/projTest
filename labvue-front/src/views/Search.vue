<template>
    <!-- 導航欄 -->
    <header class="navbar">
        <div class="logo">外送平台</div>        
        <div class="nav-links">
            <a href="#" @click.prevent="toggleRestaurantMenu">{{ isRestaurant ? '餐廳' : '餐點' }}</a>
            <a href="#">優惠</a>
            <a href="#">我的訂單</a>
            <a href="#">登入</a>
        </div>
    </header>

    <!-- 搜尋與位置區域 -->
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

    <!-- 附近熱門美食 -->
    <section class="popular-section">
        <h2>附近熱門美食</h2>
        <div class="restaurant-scroll">
            <div class="restaurant-card" v-for="restaurant in popularRestaurants" :key="restaurant.id">
                <img :src="restaurant.image" :alt="restaurant.name" />
                <div class="info">
                    <h3>{{ restaurant.name }}</h3>
                    <p>{{ restaurant.cuisine }} • {{ restaurant.deliveryTime }} 分鐘</p>
                    <p>{{ restaurant.rating }} ({{ restaurant.reviews }}+ 評價)</p>
                    <div class="tags">
                        <span v-for="tag in restaurant.tags" :key="tag">{{ tag }}</span>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- 篩選與排序（頂部） -->
    <section class="filters">
        <div class="filter-buttons">
            <button @click="applyFilter('全部')">全部</button>
            <button @click="applyFilter('中式')">中式</button>
            <button @click="applyFilter('日式')">日式</button>
            <button @click="applyFilter('西式')">西式</button>
            <button @click="applyFilter('免運費')">免運費</button>
        </div>
        <div class="sort">
            <select v-model="sortOption" @change="sortRestaurants">
                <option value="預設">排序：預設</option>
                <option value="評分最高">評分最高</option>
                <option value="距離最近">距離最近</option>
                <option value="最快送達">最快送達</option>
            </select>
        </div>
    </section>

    <!-- 內容容器 -->
    <div class="content-container">
        <!-- 左側篩選欄 -->
        <aside class="sidebar">
            <h3>篩選條件</h3>
            <div class="filter-group">
                <h4>美食類型</h4>
                <label><input type="checkbox" v-model="filters.cuisine" value="chinese"> 中式</label>
                <label><input type="checkbox" v-model="filters.cuisine" value="japanese"> 日式</label>
                <label><input type="checkbox" v-model="filters.cuisine" value="western"> 西式</label>
                <label><input type="checkbox" v-model="filters.cuisine" value="korean"> 韓式</label>
            </div>
            <div class="filter-group">
                <h4>配送時間</h4>
                <input type="range" min="0" max="60" v-model.number="filters.deliveryTime" @input="updateDeliveryTime" />
                <div class="range-value">{{ filters.deliveryTime }} 分鐘</div>
            </div>
            <div class="filter-group">
                <h4>優惠活動</h4>
                <label><input type="checkbox" v-model="filters.promo" value="free-delivery"> 免運費</label>
                <label><input type="checkbox" v-model="filters.promo" value="discount"> 折扣</label>
            </div>
            <div class="filter-group">
                <h4>評分</h4>
                <label><input type="checkbox" v-model="filters.rating" value="4"> 4 星以上</label>
                <label><input type="checkbox" v-model="filters.rating" value="3"> 3 星以上</label>
            </div>
        </aside>

        <!-- 餐廳列表 -->
        <section class="restaurant-list">
            <div class="restaurant-card" v-for="restaurant in filteredRestaurants" :key="restaurant.id">
                <img :src="restaurant.image" :alt="restaurant.name" />                
                <div class="info">
                    <h3>{{ restaurant.name }}</h3>
                    <p>{{ restaurant.cuisine }} • {{ restaurant.deliveryTime }} 分鐘 • {{ restaurant.promo || '' }}</p>
                    <p>{{ restaurant.rating }} ({{ restaurant.reviews }}+ 評價)</p>
                    <div class="tags">
                        <span v-for="tag in restaurant.tags" :key="tag">{{ tag }}</span>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- 頁腳 -->
    <footer class="footer">
        <p>© 2025 外送平台. 版權所有。</p>
        <p>
            <a href="#">關於我們</a>
            <a href="#">聯繫我們</a>
            <a href="#">隱私政策</a>
            <a href="#">服務條款</a>
        </p>
    </footer>
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

// 餐廳/餐點切換
const isRestaurant = ref(true);
const toggleRestaurantMenu = () => {
    isRestaurant.value = !isRestaurant.value;
    console.log("目前頁面餐廳為是/餐點為否:"+isRestaurant.value);
};

// 餐廳數據
const restaurants = ref([
    {
        id: 1,
        name: '美味餐廳',
        cuisine: '中式',
        deliveryTime: 25,
        rating: 4,
        reviews: 120,
        tags: ['滷肉飯', '便當'],
        image: '/image/giachi.jpg',
        promo: '免運費',
        popularityScore: 90,
    },
    {
        id: 2,
        name: '壽司之家',
        cuisine: '日式',
        deliveryTime: 10,
        rating: 5,
        reviews: 200,
        tags: ['壽司', '生魚片'],
        image: '/image/sooshi.jpg',
        promo: '',
        popularityScore: 95,
    },
    {
        id: 3,
        name: '披薩樂園',
        cuisine: '西式',
        deliveryTime: 30,
        rating: 4,
        reviews: 150,
        tags: ['披薩', '義大利麵'],
        image: '/image/pizza.jpg',
        promo: '滿 $200 免運',
        popularityScore: 85,
    },
    {
        id: 4,
        name: '韓式炸雞',
        cuisine: '韓式',
        deliveryTime: 8,
        rating: 4,
        reviews: 80,
        tags: ['炸雞', '泡菜'],
        image: '/image/fryC.jpg',
        promo: '折扣',
        popularityScore: 88,
    },
    {
        id: 5,
        name: 'haha餐廳',
        cuisine: '中式',
        deliveryTime: 25,
        rating: 4,
        reviews: 120,
        tags: ['滷肉飯', '便當'],
        image: '/image/giachi2.jpg',
        promo: '免運費',
        popularityScore: 90,
    },
    {
        id: 6,
        name: 'lala之家',
        cuisine: '日式',
        deliveryTime: 10,
        rating: 5,
        reviews: 200,
        tags: ['壽司', '生魚片'],
        image: '/image/sooshi2.jpg',
        promo: '',
        popularityScore: 95,
    },
    {
        id: 7,
        name: 'wola樂園',
        cuisine: '西式',
        deliveryTime: 30,
        rating: 4,
        reviews: 150,
        tags: ['披薩', '義大利麵'],
        image: '/image/pizza2.jpg',
        promo: '滿 $200 免運',
        popularityScore: 85,
    },
    {
        id: 8,
        name: 'GG炸雞',
        cuisine: '韓式',
        deliveryTime: 8,
        rating: 4,
        reviews: 80,
        tags: ['炸雞', '泡菜'],
        image: '/image/fryC2.jpg',
        promo: '折扣',
        popularityScore: 88,
    },
]);

// 篩選條件
const filters = ref({
    cuisine: [],
    deliveryTime: 30,
    promo: [],
    rating: [],
});

// 排序選項
const sortOption = ref('預設');

// 計算屬性：熱門餐廳（按 popularityScore 排序前 10 名）
const popularRestaurants = computed(() => {
    return [...restaurants.value]
        .sort((a, b) => b.popularityScore - a.popularityScore)
        .slice(0, 10);
});

// 計算屬性：篩選後的餐廳列表
const filteredRestaurants = computed(() => {
    let filtered = [...restaurants.value];

    // 篩選美食類型
    if (filters.value.cuisine.length > 0) {
        filtered = filtered.filter(restaurant =>
            filters.value.cuisine.includes(restaurant.cuisine.toLowerCase())
        );
    }

    // 篩選配送時間
    filtered = filtered.filter(restaurant => restaurant.deliveryTime <= filters.value.deliveryTime);

    // 篩選優惠
    if (filters.value.promo.length > 0) {
        filtered = filtered.filter(restaurant =>
            filters.value.promo.some(promo =>
                restaurant.promo.toLowerCase().includes(promo)
            )
        );
    }

    // 篩選評分
    if (filters.value.rating.length > 0) {
        filtered = filtered.filter(restaurant =>
            filters.value.rating.some(rating => restaurant.rating >= parseInt(rating))
        );
    }

    // 排序
    if (sortOption.value === '評分最高') {
        filtered = filtered.sort((a, b) => b.rating - a.rating);
    } else if (sortOption.value === '距離最近' || sortOption.value === '最快送達') {
        filtered = filtered.sort((a, b) => a.deliveryTime - b.deliveryTime);
    }

    return filtered;
});

// 應用頂部篩選按鈕
const applyFilter = (filterType) => {
    if (filterType === '全部') {
        filters.value.cuisine = [];
        filters.value.promo = [];
    } else if (['中式', '日式', '西式', '韓式'].includes(filterType)) {
        const cuisineLower = filterType.toLowerCase();
        if (!filters.value.cuisine.includes(cuisineLower)) {
            filters.value.cuisine.push(cuisineLower);
        }
    } else if (filterType === '免運費') {
        if (!filters.value.promo.includes('free-delivery')) {
            filters.value.promo.push('free-delivery');
        }
    }
};

// 更新配送時間
const updateDeliveryTime = () => {
    // 觸發篩選更新（由 computed 自動處理）
};



// 行動裝置篩選欄切換
onMounted(() => {
    const toggleButton = document.createElement('div');
    toggleButton.className = 'filter-toggle';
    toggleButton.textContent = '篩選條件';
    const sidebar = document.querySelector('.sidebar');
    const filtersSection = document.querySelector('.filters');
    filtersSection.insertAdjacentElement('beforebegin', toggleButton);

    toggleButton.addEventListener('click', () => {
        sidebar.classList.toggle('active');
    });
});
</script>

<style>
/* 您的原始 CSS 樣式保持不變 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
    font-family: Arial, sans-serif;
}

body {
    background-color: #f7f7f7;
}

/* 導航欄 */
.navbar {
    background-color: #d70f64;
    color: white;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.navbar .logo {
    font-size: 26px;
    font-weight: bold;
}

.navbar .nav-links {
    display: flex;
    align-items: center;
    gap: 20px;
}

.navbar .nav-links a {
    color: white;
    text-decoration: none;
    font-size: 16px;
}

.navbar .location {
    display: flex;
    align-items: center;
    gap: 10px;
}

.navbar input {
    padding: 8px;
    border: none;
    border-radius: 4px;
    width: 200px;
}

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

/* 附近熱門美食 */
.popular-section {
    padding: 10px;
    background-color: #fff;
    margin: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.popular-section h2 {
    font-size: 12px;
    margin-bottom: 7.5px;
    color: #333;
}

.restaurant-scroll {
    display: flex;
    overflow-x: auto;
    gap: 10px;
    padding-bottom: 5px;
    scroll-behavior: smooth;
}

.restaurant-scroll::-webkit-scrollbar {
    height: 4px;
}

.restaurant-scroll::-webkit-scrollbar-thumb {
    background-color: #d70f64;
    border-radius: 4px;
}

.restaurant-scroll .restaurant-card {
    flex: 0 0 140px;
    min-width: 140px;
}

.restaurant-card img {
    width: 100%;
    height: 80px;
    object-fit: cover;
}

.restaurant-card .info {
    padding: 7.5px;
}

.restaurant-card h3 {
    font-size: 9px;
    margin-bottom: 4px;
}

.restaurant-card p {
    color: #666;
    font-size: 7px;
    margin-bottom: 2.5px;
}

.restaurant-card .tags {
    display: flex;
    gap: 5px;
    margin-top: 5px;
}

.restaurant-card .tags span {
    background-color: #f0f0f0;
    padding: 2.5px 5px;
    border-radius: 6px;
    font-size: 6px;
}

/* 篩選與排序（頂部） */
.filters {
    padding: 15px 20px;
    background-color: #fff;
    margin: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filters .filter-buttons button {
    padding: 8px 16px;
    border: 1px solid #ccc;
    background-color: #fff;
    cursor: pointer;
    border-radius: 20px;
    margin-right: 10px;
    font-size: 14px;
}

.filters .filter-buttons button:hover {
    background-color: #d70f64;
    color: white;
}

.filters .sort select {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 14px;
}

/* 內容容器 */
.content-container {
    display: flex;
    padding: 20px;
    gap: 20px;
}

/* 左側篩選欄 */
.sidebar {
    width: 250px;
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.sidebar h3 {
    font-size: 18px;
    margin-bottom: 15px;
}

.filter-group {
    margin-bottom: 20px;
}

.filter-group label {
    display: block;
    margin-bottom: 10px;
    font-size: 14px;
    color: #333;
}

.filter-group input[type="checkbox"] {
    margin-right: 8px;
}

.filter-group input[type="range"] {
    width: 100%;
}

.filter-group .range-value {
    font-size: 14px;
    color: #666;
    margin-top: 5px;
}

/* 餐廳列表 */
.restaurant-list {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.restaurant-card {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;
}

.restaurant-card:hover {
    transform: scale(1.02);
}

.restaurant-card img {
    width: 100%;
    height: 160px;
    object-fit: cover;
}

.restaurant-card .info {
    padding: 15px;
}

.restaurant-card h3 {
    font-size: 18px;
    margin-bottom: 8px;
}

.restaurant-card p {
    color: #666;
    font-size: 14px;
    margin-bottom: 5px;
}

.restaurant-card .tags {
    display: flex;
    gap: 10px;
    margin-top: 10px;
}

.restaurant-card .tags span {
    background-color: #f0f0f0;
    padding: 5px 10px;
    border-radius: 12px;
    font-size: 12px;
}

/* 頁腳 */
.footer {
    background-color: #333;
    color: white;
    padding: 20px;
    text-align: center;
    margin-top: 30px;
}

.footer a {
    color: #d70f64;
    text-decoration: none;
    margin: 0 10px;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .content-container {
        flex-direction: column;
    }

    .sidebar {
        width: 100%;
        display: none;
    }

    .sidebar.active {
        display: block;
    }

    .filter-toggle {
        display: block;
        padding: 10px;
        background-color: #d70f64;
        color: white;
        text-align: center;
        cursor: pointer;
        margin: 10px 20px;
        border-radius: 4px;
    }

    .restaurant-scroll {
        flex-wrap: nowrap;
    }

    .popular-section,
    .filters,
    .hero-section {
        margin: 10px 10px 20px;
    }
}

@media (min-width: 769px) {
    .filter-toggle {
        display: none;
    }
}
</style>