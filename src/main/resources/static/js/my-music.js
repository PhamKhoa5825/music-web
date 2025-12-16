/**
 * my-music.js - Final Fixed Version
 * Fix lỗi: Grid không hiện dữ liệu & Kiểm tra trùng tên Playlist
 */

const USER_ID = localStorage.getItem('music_user_id') || 1;
const API = "/api";

// --- GLOBAL VARIABLES ---
let sidebarPlaylistsData = []; // Cache dữ liệu playlist
let isDataLoaded = false;      // Cờ kiểm tra xem đã load dữ liệu lần đầu chưa
let isSidebarExpanded = false; // Trạng thái đóng/mở sidebar

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('display-uid').innerText = USER_ID;

    // 1. Setup Navigation & Events trước
    setupNavigation();
    setupModalEvents();

    // 2. INIT: Load dữ liệu và hiển thị giao diện ban đầu
    initDataAndUI();
});

// --- CORE: HÀM QUẢN LÝ DỮ LIỆU TẬP TRUNG (FIX XUNG ĐỘT) ---
async function initDataAndUI() {
    await fetchPlaylistsData(); // Chờ tải xong dữ liệu

    // Render Sidebar ngay lập tức
    renderSidebarUI();

    // Check URL xem đang ở tab nào để render tab đó
    const urlParams = new URLSearchParams(window.location.search);
    const tab = urlParams.get('tab') || 'foryou';
    switchView(tab);
}

// Hàm chỉ chuyên trách việc gọi API và lưu vào biến toàn cục
async function fetchPlaylistsData() {
    try {
        const res = await fetch(`${API}/playlists/user/${USER_ID}`);
        if(res.ok) {
            const data = await res.json();
            // Sắp xếp: Mới nhất lên đầu (ID giảm dần)
            sidebarPlaylistsData = data.sort((a, b) => b.playlistId - a.playlistId);
            isDataLoaded = true;
        }
    } catch(e) { console.error("Lỗi tải playlist:", e); }
}

// --- NAVIGATION ---
function setupNavigation() {
    bindTabClick('nav-foryou', 'foryou');
    bindTabClick('nav-liked', 'liked');
    bindTabClick('nav-history', 'history');
    bindTabClick('nav-playlists', 'playlists');
}

function bindTabClick(elementId, viewName) {
    const el = document.getElementById(elementId);
    if (el) {
        el.addEventListener('click', (e) => {
            e.preventDefault();
            window.history.pushState({tab: viewName}, '', `/my-music?tab=${viewName}`);
            switchView(viewName);
        });
    }
}

function switchView(viewName) {
    // UI Update
    document.querySelectorAll('.content-view').forEach(el => el.classList.add('d-none'));
    document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active-tab', 'text-white'));

    const view = document.getElementById(`view-${viewName}`);
    if (view) view.classList.remove('d-none');

    const link = document.getElementById(`nav-${viewName}`);
    if (link) link.classList.add('active-tab', 'text-white');

    // Title Update
    const titles = {
        'foryou': 'Dành cho bạn', 'liked': 'Bài hát yêu thích',
        'history': 'Lịch sử nghe', 'playlists': 'Playlist của tôi'
    };
    document.getElementById('page-title').innerText = titles[viewName] || 'Thư viện';

    // Lazy Load Data
    if(viewName === 'foryou') loadForYouData();
    if(viewName === 'liked') loadLikedSongs();
    if(viewName === 'history') loadHistory();

    // [FIX] Luôn gọi render Grid, hàm này sẽ tự check dữ liệu có chưa
    if(viewName === 'playlists') renderUserPlaylistsGrid();
}

// --- UI RENDER: SIDEBAR (LOGIC CŨ + XEM THÊM) ---
function renderSidebarUI() {
    const container = document.getElementById('sidebar-playlists');
    const btnExpand = document.getElementById('btn-sidebar-expand');
    if(!container) return;

    container.innerHTML = '';

    // Logic cắt danh sách
    const limit = isSidebarExpanded ? sidebarPlaylistsData.length : 5;
    const displayList = sidebarPlaylistsData.slice(0, limit);

    displayList.forEach(p => {
        container.innerHTML += `
            <a href="/playlist/${p.playlistId}" class="nav-link text-truncate py-2 text-white-50" style="font-size: 0.95rem;">
                <i class="bi bi-music-note-list"></i> ${p.name}
            </a>`;
    });

    // Nút Xem thêm
    if (sidebarPlaylistsData.length > 5) {
        btnExpand.classList.remove('d-none');
        btnExpand.innerHTML = isSidebarExpanded
            ? 'Thu gọn <i class="bi bi-chevron-up ms-1"></i>'
            : `Xem thêm (${sidebarPlaylistsData.length - 5}) <i class="bi bi-chevron-down ms-1"></i>`;
    } else {
        btnExpand.classList.add('d-none');
    }
}

document.getElementById('btn-sidebar-expand').addEventListener('click', () => {
    isSidebarExpanded = !isSidebarExpanded;
    renderSidebarUI();
});

// --- UI RENDER: GRID TAB (FIX LỖI KHÔNG HIỆN DỮ LIỆU) ---
async function renderUserPlaylistsGrid() {
    const container = document.getElementById('user-playlists-grid');
    const countSpan = document.getElementById('playlist-total-count');

    // [QUAN TRỌNG] Nếu dữ liệu chưa có (do user F5 thẳng vào tab playlist), thì gọi API
    if (!isDataLoaded || sidebarPlaylistsData.length === 0) {
        container.innerHTML = '<div class="text-white-50 ms-2">Đang tải dữ liệu...</div>';
        await fetchPlaylistsData();
    }

    // Sau khi đảm bảo data đã có thì render
    if(countSpan) countSpan.innerText = `${sidebarPlaylistsData.length} tuyển tập`;
    container.innerHTML = '';

    if(sidebarPlaylistsData.length === 0) {
        container.innerHTML = '<div class="text-white-50 ms-2">Bạn chưa tạo playlist nào.</div>';
        return;
    }

    sidebarPlaylistsData.forEach(p => {
        const img = p.imageUrl || `https://placehold.co/300x300/333/fff?text=${p.name.charAt(0).toUpperCase()}`;
        const badge = p.isPublic
            ? '<span class="badge bg-success position-absolute top-0 end-0 m-2 shadow">Public</span>'
            : '<span class="badge bg-secondary position-absolute top-0 end-0 m-2 shadow">Private</span>';

        container.innerHTML += `
            <div class="col">
                <div class="custom-card h-100" onclick="window.location.href='/playlist/${p.playlistId}'">
                    <div style="position: relative;">
                        <img src="${img}" alt="${p.name}" class="w-100 rounded shadow-sm mb-3" style="aspect-ratio: 1/1; object-fit: cover;">
                        ${badge}
                        <div class="play-btn-hover"><i class="bi bi-music-note-list"></i></div>
                    </div>
                    <h6 class="text-white text-truncate mb-1 fw-bold" title="${p.name}">${p.name}</h6>
                    <div class="text-white-50 small text-truncate">Của bạn</div>
                </div>
            </div>`;
    });
}

// --- LOGIC TẠO PLAYLIST (FIX TRÙNG TÊN) ---
function setupModalEvents() {
    const btnSidebar = document.getElementById('btn-create-playlist');
    if(btnSidebar) btnSidebar.addEventListener('click', openCreateModal);

    const btnTab = document.getElementById('btn-create-playlist-tab');
    if(btnTab) btnTab.addEventListener('click', openCreateModal);

    document.getElementById('btn-submit-create-pl').addEventListener('click', createPlaylist);
}

function openCreateModal() {
    document.getElementById('newPlName').value = '';
    // Mặc định công khai
    const chk = document.getElementById('isPublicCheck');
    if(chk) { chk.checked = true; updatePublicLabel(chk); }

    new bootstrap.Modal(document.getElementById('createPlModal')).show();
}

function updatePublicLabel(chk) {
    const label = document.getElementById('label-public');
    if(chk.checked) {
        label.innerText = "Công khai";
        label.className = "fw-bold text-success d-block";
    } else {
        label.innerText = "Riêng tư";
        label.className = "fw-bold text-danger d-block";
    }
}
// Sự kiện toggle switch
document.getElementById('isPublicCheck').addEventListener('change', (e) => updatePublicLabel(e.target));

async function createPlaylist() {
    const nameInput = document.getElementById('newPlName');
    const name = nameInput.value.trim();
    const isPublic = document.getElementById('isPublicCheck').checked;

    if(!name) return alert("Vui lòng nhập tên Playlist!");

    // [FIX] KIỂM TRA TRÙNG TÊN (CLIENT SIDE)
    // Duyệt qua mảng sidebarPlaylistsData để check
    const isDuplicate = sidebarPlaylistsData.some(p => p.name.toLowerCase() === name.toLowerCase());
    if (isDuplicate) {
        alert(`Playlist tên "${name}" đã tồn tại! Vui lòng chọn tên khác.`);
        nameInput.focus();
        return; // Dừng lại, không gọi API
    }

    try {
        const res = await fetch(`${API}/playlists`, {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({ userId: USER_ID, name: name, isPublic: isPublic })
        });

        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('createPlModal')).hide();

            // Reload lại dữ liệu từ server để đảm bảo đồng bộ ID
            await fetchPlaylistsData();

            // Cập nhật cả 2 giao diện
            renderSidebarUI();

            // Nếu đang mở tab Playlist thì render lại luôn
            const urlParams = new URLSearchParams(window.location.search);
            if(urlParams.get('tab') === 'playlists') {
                renderUserPlaylistsGrid();
            }

            alert("Tạo playlist thành công!");
        } else {
            alert("Lỗi server khi tạo playlist");
        }
    } catch (e) { console.error(e); }
}

// --- CÁC HÀM CŨ (KHÔNG THAY ĐỔI) ---
async function loadForYouData() { /* (Giữ nguyên code ForYou cũ) */
    const containerMix = document.getElementById('foryou-mix-list');
    if(containerMix.children.length > 1 && !containerMix.querySelector('.spinner-border')) return;
    try {
        const res = await fetch(`${API}/foryou/${USER_ID}`);
        const data = await res.json();
        renderCards(data.dailyMix, 'foryou-mix-list', false);
        renderCards(data.trending, 'foryou-trend-list', true);
        renderCards(data.discovery, 'foryou-discovery-list', false);
    } catch(e) { console.error(e); }
}

async function loadLikedSongs() { /* (Giữ nguyên code Liked cũ) */
    try {
        const res = await fetch(`${API}/favorites/user/${USER_ID}`);
        if(res.ok) {
            const songs = await res.json();
            document.getElementById('liked-count-text').innerText = `${songs.length} bài hát`;
            renderCards(songs, 'liked-songs-grid', false);
        }
    } catch(e) { console.error(e); }
}

async function loadHistory() { /* (Giữ nguyên code History cũ) */
    try {
        const res = await fetch(`${API}/history/user/${USER_ID}`);
        const data = await res.json();
        const tbody = document.getElementById('history-body');
        document.getElementById('history-count').innerText = `(${data.length})`;
        tbody.innerHTML = '';
        if(data.length === 0) { tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Chưa có lịch sử</td></tr>'; return; }
        data.forEach((h, idx) => {
            const s = h.song;
            const img = s.coverImage || 'https://placehold.co/40';
            tbody.innerHTML += `
            <tr>
                <td class="text-white-50 align-middle">${idx + 1}</td>
                <td class="align-middle">
                    <div class="d-flex align-items-center">
                        <div class="position-relative me-3" style="width: 40px; height: 40px; cursor: pointer;" onclick="playSongGlobal(event, ${s.songId})">
                             <img src="${img}" class="rounded w-100 h-100 object-fit-cover">
                        </div>
                        <div role="button" onclick="window.location.href='/song/${s.songId}'">
                            <div class="fw-bold text-white text-truncate">${s.title}</div>
                        </div>
                    </div>
                </td>
                <td class="align-middle text-white-50">${s.artist ? s.artist.name : 'Unknown'}</td>
                <td class="align-middle text-end text-white-50 small">--:--</td>
                <td class="align-middle"></td>
            </tr>`;
        });
    } catch(e) { console.error(e); }
}

// Render Card Helper
function renderCards(songs, containerId, isRanking) {
    const div = document.getElementById(containerId);
    div.innerHTML = '';
    if(!songs || songs.length === 0) { div.innerHTML = '<div class="text-white-50 ms-2">Chưa có dữ liệu...</div>'; return; }
    songs.forEach((s, index) => {
        const img = s.coverImage && s.coverImage.startsWith('http') ? s.coverImage : `https://placehold.co/300?text=${s.title.charAt(0)}`;
        let badgeHtml = '';
        if (isRanking) {
            let rankClass = index === 0 ? 'bg-danger' : (index === 1 ? 'bg-success' : (index === 2 ? 'bg-info' : 'bg-secondary'));
            badgeHtml = `<span class="position-absolute top-0 start-0 m-2 badge ${rankClass} shadow" style="z-index: 5;">#${index + 1}</span>`;
        }
        div.innerHTML += `
            <div class="col">
                <div class="custom-card h-100" onclick="window.location.href='/song/${s.songId}'">
                    ${badgeHtml}
                    <div style="position: relative;">
                        <img src="${img}" alt="${s.title}" loading="lazy" class="w-100 rounded shadow-sm mb-3">
                        <div class="play-btn-hover" onclick="playSongGlobal(event, ${s.songId})"><i class="bi bi-play-fill"></i></div>
                    </div>
                    <h6 class="text-white text-truncate mb-1 fw-bold">${s.title}</h6>
                    <div class="text-white-50 small text-truncate">${s.artist ? s.artist.name : 'Unknown'}</div>
                </div>
            </div>`;
    });
}


// --- PLAYER INTEGRATION (GỘP CODE VỚI NGƯỜI 2) ---
function playSongGlobal(event, songId) {
    // Ngăn chặn sự kiện click lan ra thẻ cha (thẻ cha chuyển trang detail)
    event.stopPropagation();
    event.preventDefault();

    console.log(`[PLAYER] Request play song ID: ${songId}`);

    // Gửi log lên server
    fetch(`${API}/history?userId=${USER_ID}&songId=${songId}`, {method: 'POST'});

    // TODO: Gọi hàm phát nhạc của Người 2 tại đây
    // Ví dụ: MusicPlayer.play(songId);
    alert(`Đang phát nhạc (ID: ${songId})... (Placeholder cho Player)`);
}