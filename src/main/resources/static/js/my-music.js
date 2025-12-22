/**
 * my-music.js - ULTIMATE FIX VERSION
 * Fix: Add to Playlist (Checkbox), Missing Tabs Data, Song Detail UI Restoration, Playlist Edit
 */

const USER_ID = localStorage.getItem('music_user_id') || 1;
const API = "/api";

// --- GLOBAL STATE ---
let sidebarPlaylistsData = [];
let isSidebarExpanded = false;
let currentRankMode = 'WEEK';
let searchTimeout = null;
let currentPlaylistId = null; // ID Playlist đang xem/sửa
let targetSongIdForAdd = null; // ID bài hát đang muốn thêm vào playlist

// --- 1. CORE: INITIALIZATION & ROUTING ---
document.addEventListener('DOMContentLoaded', async () => {
    // Setup UI
    const uidEl = document.getElementById('display-uid');
    if(uidEl) uidEl.innerText = USER_ID;

    // Check Admin & Load Data
    await Promise.all([checkAdminRole(), loadSidebarPlaylists()]);

    // Setup Events
    setupGlobalEvents();

    // Start Routing
    handleRouting();
});

window.onpopstate = () => handleRouting();

// Routing logic: Chỉ ẩn/hiện các div trong .main-content
function handleRouting() {
    const path = window.location.pathname;
    const params = new URLSearchParams(window.location.search);

    // Xử lý URL
    if (path.startsWith('/song/')) {
        const sid = path.split('/').pop();
        if(sid) { switchView('song-detail', sid); return; }
    }
    if (path.startsWith('/playlist/')) {
        const pid = path.split('/').pop();
        if(pid) { switchView('playlist-detail', pid); return; }
    }
    if (path.includes('/ranking')) { switchView('ranking'); return; }

    const tab = params.get('tab');
    if (tab) { switchView(tab); return; }

    switchView('home');
}

function switchView(viewName, idParam = null) {
    // 1. Ẩn tất cả views
    document.querySelectorAll('.content-view').forEach(el => el.classList.add('d-none'));
    document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active-tab', 'text-white'));

    // 2. Active View đích
    // Đảm bảo trong HTML bạn đã có: <div id="view-ai-result" class="content-view d-none">...</div>
    let targetId = `view-${viewName}`;
    const target = document.getElementById(targetId);
    if (target) target.classList.remove('d-none');

    // 3. Active Sidebar tab
    const navId = `nav-${viewName}`;
    const navEl = document.getElementById(navId);
    if(navEl) navEl.classList.add('active-tab', 'text-white');

    // 4. Update URL
    let newUrl = '/my-music';
    if(viewName === 'home') newUrl = '/home';
    else if(viewName === 'ranking') newUrl = '/ranking';
    else if(viewName === 'playlist-detail') newUrl = `/playlist/${idParam}`;
    else if(viewName === 'song-detail') newUrl = `/song/${idParam}`;
    else if(viewName === 'ai-result') newUrl = `/ai-dj`; // [NEW] Thêm dòng này để URL đẹp hơn
    else if (viewName !== 'home') newUrl = `/my-music?tab=${viewName}`;

    if(window.location.pathname + window.location.search !== newUrl) {
        // Lưu lại state để khi back lại không bị lỗi
        window.history.pushState({view: viewName, id: idParam}, '', newUrl);
    }

    // 5. Load Data cho View
    loadDataForView(viewName, idParam);
}

function loadDataForView(view, id) {
    // Ẩn dropdown tìm kiếm khi chuyển trang
    const searchRes = document.getElementById('searchResults');
    if(searchRes) searchRes.style.display = 'none';

    switch (view) {
        case 'home': loadHomeData(); break;
        case 'ranking': loadRankingData(); break;
        case 'foryou': loadForYouData(); break;
        case 'liked': loadLikedSongs(); break;
        case 'history': loadHistoryData(); break;
        case 'playlists': renderUserPlaylistsGrid(); break;
        case 'playlist-detail': if(id) loadPlaylistDetail(id); break;
        case 'song-detail': if(id) loadSongDetailSPA(id); break;
    }
}

// --- 2. GLOBAL SEARCH & EVENTS ---
async function checkAdminRole() {
    try {
        const res = await fetch(`${API}/users/${USER_ID}`);
        if (res.ok) {
            const user = await res.json();
            // Nếu là ADMIN, hiện nút vào Dashboard ở Header (Home)
            if(user.role === 'ADMIN') {
                const homeHeader = document.querySelector('#view-home .d-flex.align-items-center.gap-3');
                if(homeHeader) {
                    const btn = document.createElement('a');
                    btn.href = '/admin/dashboard';
                    btn.className = 'btn-admin-access me-2';
                    btn.innerHTML = '<i class="bi bi-shield-lock-fill me-1"></i>ADMIN';
                    homeHeader.prepend(btn);
                }
            }
        }
    } catch (e) {}
}

function setupGlobalEvents() {
    // 1. Sidebar Expand
    document.getElementById('btn-sidebar-expand')?.addEventListener('click', () => {
        isSidebarExpanded = !isSidebarExpanded; renderSidebarUI();
    });

    // 2. Button Tạo Playlist (Từ Sidebar hoặc Modal)
    // [FIX] Gán sự kiện cho cả nút trong Sidebar và nút Submit trong Modal
    const btnCreateSidebar = document.getElementById('btn-create-playlist');
    if(btnCreateSidebar) {
        btnCreateSidebar.addEventListener('click', () => {
            document.getElementById('newPlName').value = '';
            new bootstrap.Modal(document.getElementById('createPlModal')).show();
        });
    }
    document.getElementById('btn-submit-create-pl')?.addEventListener('click', createPlaylist);

    // 3. Global Search Input
    const searchInput = document.getElementById('searchInput');
    if(searchInput) {
        searchInput.addEventListener('keyup', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => doGlobalSearch(e.target.value), 400);
        });
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.position-relative'))
                document.getElementById('searchResults').style.display = 'none';
        });
    }

    // 4. Change User (Click Avatar)
    document.querySelector('.top-header img')?.addEventListener('click', changeUser);
}

async function doGlobalSearch(keyword) {
    const resultBox = document.getElementById('searchResults');
    if(!keyword.trim()) { resultBox.style.display = 'none'; return; }

    try {
        const res = await fetch(`${API}/search?keyword=${encodeURIComponent(keyword)}`);
        const data = await res.json();

        let html = '';
        if(data.songs && data.songs.length > 0) {
            html += '<div class="text-muted small fw-bold px-2 py-1">BÀI HÁT</div>';
            data.songs.forEach(s => {
                html += `
                <div class="search-item d-flex align-items-center p-2 rounded" onclick="navigateToSong(${s.songId})">
                    <img src="${s.coverImage}" width="40" height="40" class="rounded me-3 object-fit-cover">
                    <div>
                        <div class="text-white fw-bold small">${s.title}</div>
                        <div class="text-white-50 small" style="font-size: 0.75rem">${s.artist?.name}</div>
                    </div>
                </div>`;
            });
        }
        if(data.playlists && data.playlists.length > 0) {
            html += '<div class="text-muted small fw-bold px-2 py-1 mt-2">PLAYLIST</div>';
            data.playlists.forEach(p => {
                html += `
                <div class="search-item d-flex align-items-center p-2 rounded" onclick="switchView('playlist-detail', ${p.playlistId})">
                    <img src="${p.imageUrl || 'https://placehold.co/40'}" width="40" height="40" class="rounded me-3 object-fit-cover">
                    <div class="text-white fw-bold small">${p.name}</div>
                </div>`;
            });
        }

        if(html === '') html = '<div class="p-3 text-center text-muted">Không tìm thấy kết quả</div>';
        resultBox.innerHTML = html;
        resultBox.style.display = 'block';
    } catch(e) { console.error(e); }
}

function changeUser() {
    let u = prompt("Nhập User ID để chuyển tài khoản:", USER_ID);
    if(u && u !== USER_ID) {
        localStorage.setItem('music_user_id', u);
        location.reload();
    }
}

// --- 3. DATA LOADERS & FEATURES ---

// A. PLAYLIST LOGIC (Create, Edit, Delete, Add/Remove Song)
async function createPlaylist() {
    const name = document.getElementById('newPlName').value.trim();
    const isPublic = document.getElementById('isPublicCheck').checked;

    if(!name) return alert("Vui lòng nhập tên Playlist");

    // Check trùng tên (Client-side)
    const isDuplicate = sidebarPlaylistsData.some(p => p.name.toLowerCase() === name.toLowerCase());
    if(isDuplicate) {
        alert("Bạn đã có Playlist tên này rồi. Vui lòng chọn tên khác!");
        return;
    }

    try {
        await fetch(`${API}/playlists`, {
            method: 'POST', headers: {'Content-Type':'application/json'},
            body: JSON.stringify({ userId: USER_ID, name: name, isPublic: isPublic })
        });
        alert("Tạo thành công!");
        const modalEl = document.getElementById('createPlModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();
        loadSidebarPlaylists(); // Reload sidebar
    } catch (e) { alert("Lỗi tạo playlist: " + e.message); }
}

async function loadPlaylistDetail(pid) {
    currentPlaylistId = pid;
    const container = document.getElementById('view-playlist-detail');
    container.innerHTML = `<div class="text-center pt-5"><div class="spinner-border text-info"></div></div>`;

    try {
        const res = await fetch(`${API}/playlists/${pid}`);
        if(!res.ok) throw new Error("Not found");
        const pl = await res.json();

        // Render Header & List
        const img = pl.imageUrl || `https://placehold.co/300`;
        const bg = pl.backgroundImage || img;

        container.innerHTML = `
        <div class="playlist-header mb-4" style="height: 350px; position: relative; overflow: hidden; border-radius: 8px;">
            <div style="position: absolute; inset: 0; background-image: url('${bg}'); background-size: cover; filter: blur(20px) brightness(0.5);"></div>
            <div class="d-flex align-items-end h-100 p-4 position-relative" style="z-index: 2; background: linear-gradient(to top, #121212, transparent);">
                <img src="${img}" class="shadow rounded me-4" width="220" height="220" style="object-fit: cover;">
                <div class="mb-2">
                    <span class="badge bg-primary mb-2">PLAYLIST</span>
                    <h1 class="display-4 fw-bold mb-2 text-white">${pl.name}</h1>
                    <p class="text-white-50">${pl.description || 'Chưa có mô tả'}</p>
                    <div class="d-flex gap-2">
                        <button class="btn btn-success rounded-pill px-4 fw-bold"><i class="bi bi-play-fill me-1"></i> Phát</button>
                        <button class="btn btn-outline-light rounded-pill px-4" 
                                onclick="openEditPlaylistModal('${pl.name}', '${pl.description||''}', ${pl.isPublic})">
                            <i class="bi bi-pencil-fill me-2"></i> Sửa
                        </button>
                        <button class="btn btn-outline-danger rounded-pill px-3" onclick="deletePlaylist(${pl.playlistId})"><i class="bi bi-trash-fill"></i></button>
                    </div>
                </div>
            </div>
        </div>
        <div class="px-4">
             <table class="table table-borderless table-hover align-middle text-white mb-5">
                <thead class="text-secondary small border-bottom border-secondary">
                    <tr><th>#</th><th>BÀI HÁT</th><th>NGHỆ SĨ</th><th class="text-end"></th></tr>
                </thead>
                <tbody>
                     ${pl.playlistSongs.map((item, idx) => `
                        <tr class="group-action">
                            <td class="text-white-50 text-center">${idx+1}</td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <img src="${item.song.coverImage}" class="rounded me-3" width="40" height="40">
                                    <div class="cursor-pointer" onclick="navigateToSong(${item.song.songId})">
                                        <div class="fw-bold text-white">${item.song.title}</div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-white-50">${item.song.artist?.name}</td>
                            <td class="text-end">
                                <i class="bi bi-x-lg text-danger cursor-pointer hover-visible" onclick="removeSongFromPlaylist(event, ${pl.playlistId}, ${item.song.songId})"></i>
                            </td>
                        </tr>
                     `).join('')}
                </tbody>
            </table>
            <div class="search-add-section pb-5" style="max-width: 600px;">
                <h5 class="fw-bold mb-3">Thêm bài hát</h5>
                <input type="text" class="form-control bg-dark text-white border-secondary rounded-pill py-2 ps-4" 
                       placeholder="Tìm kiếm..." onkeyup="handleSearchAddSong(this.value, ${pl.playlistId})">
                <div id="pl-search-results" class="mt-2 list-group"></div>
            </div>
        </div>`;
    } catch(e) { container.innerHTML = `<div class="p-5 text-center text-danger">Lỗi: ${e.message}</div>`; }
}

async function deletePlaylist(pid) {
    if(!confirm("Bạn có chắc chắn muốn XÓA VĨNH VIỄN playlist này?")) return;
    try {
        const res = await fetch(`${API}/playlists/${pid}`, { method: 'DELETE' });
        if(res.ok) {
            alert("Đã xóa playlist");
            loadSidebarPlaylists();
            switchView('home');
        } else alert("Lỗi khi xóa playlist");
    } catch(e) { console.error(e); }
}

// Logic Edit
function openEditPlaylistModal(pid, name, desc, isPublic) {
    const modalEl = document.getElementById('editDetailsModal');
    // Fill data
    document.getElementById('edit-name').value = name;
    document.getElementById('edit-desc').value = desc;
    document.getElementById('edit-isPublic').checked = isPublic;
    modalEl.dataset.pid = pid; // Lưu pid vào dataset để nút Lưu biết

    // Gán lại sự kiện onclick cho nút Lưu trong modal này (nếu cần, hoặc dùng hàm savePlaylistDetails chung)
    const btnSave = modalEl.querySelector('button[onclick*="savePlaylistDetails"]');
    if(btnSave) btnSave.onclick = savePlaylistDetails;

    new bootstrap.Modal(modalEl).show();
}

async function savePlaylistDetails() {
    const name = document.getElementById('edit-name').value;
    const desc = document.getElementById('edit-desc').value;
    const isPublic = document.getElementById('edit-isPublic').checked;

    if(!currentPlaylistId) return;

    try {
        await fetch(`${API}/playlists/${currentPlaylistId}`, {
            method: 'PATCH', headers: {'Content-Type':'application/json'},
            body: JSON.stringify({name, description: desc, isPublic})
        });
        // Ẩn modal
        bootstrap.Modal.getInstance(document.getElementById('editDetailsModal')).hide();
        // Reload dữ liệu
        loadPlaylistDetail(currentPlaylistId);
        loadSidebarPlaylists();
    } catch(e) { alert("Lỗi cập nhật!"); }
}

// Logic: Search Add Song trong Playlist Detail
function handleSearchAddSong(keyword, pid) {
    clearTimeout(searchTimeout);
    const box = document.getElementById('pl-search-results');
    if(!keyword.trim()) { box.innerHTML = ''; return; }

    searchTimeout = setTimeout(async () => {
        const res = await fetch(`${API}/playlists/search-songs?keyword=${encodeURIComponent(keyword)}`);
        const songs = await res.json();
        box.innerHTML = '';
        songs.forEach(s => {
            box.innerHTML += `
                <div class="list-group-item bg-dark border-secondary text-white d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <img src="${s.coverImage}" width="40" class="rounded me-3">
                        <div>
                            <div class="fw-bold">${s.title}</div>
                            <small class="text-white-50">${s.artist?.name}</small>
                        </div>
                    </div>
                    <button class="btn btn-sm btn-outline-light rounded-pill" onclick="addSongToPlaylist(${pid}, ${s.songId})">Thêm</button>
                </div>`;
        });
    }, 400);
}

async function addSongToPlaylist(pid, sid) {
    await fetch(`${API}/playlists/${pid}/songs`, {
        method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({songId: sid})
    });
    loadPlaylistDetail(pid);
}

async function removeSongFromPlaylist(e, pid, sid) {
    e.stopPropagation();
    if(!confirm("Xóa bài hát?")) return;
    await fetch(`${API}/playlists/${pid}/songs/${sid}`, {method: 'DELETE'});
    loadPlaylistDetail(pid);
}

function renderPlaylistSongs(list, pid) {
    if(!list || list.length === 0) return '<tr><td colspan="5" class="text-center py-4 text-muted">Playlist trống</td></tr>';
    return list.map((item, idx) => {
        const s = item.song;
        return `
        <tr class="group-action">
            <td class="text-white-50 text-center">${idx+1}</td>
            <td>
                <div class="d-flex align-items-center">
                    <img src="${s.coverImage}" class="rounded me-3" width="40" height="40">
                    <div class="cursor-pointer" onclick="navigateToSong(${s.songId})">
                        <div class="fw-bold text-white">${s.title}</div>
                    </div>
                </div>
            </td>
            <td class="text-white-50">${s.artist ? s.artist.name : ''}</td>
            <td class="text-end text-white-50">04:20</td>
            <td class="text-end">
                <i class="bi bi-x-lg text-danger cursor-pointer hover-visible" 
                   onclick="removeSongFromPlaylist(event, ${pid}, ${s.songId})" title="Xóa"></i>
            </td>
        </tr>`;
    }).join('');
}


// B. TAB LOADERS: FOR YOU & LIKED (FIXED)
async function loadForYouData() {
    const mixList = document.getElementById('foryou-mix-list');
    const trendList = document.getElementById('foryou-trend-list');
    const discList = document.getElementById('foryou-discovery-list');

    // [FIX] Kiểm tra nếu container tồn tại
    if(!mixList || !trendList || !discList) return;

    try {
        const res = await fetch(`${API}/foryou/${USER_ID}`);
        const data = await res.json();

        renderCardGrid(data.dailyMix, 'foryou-mix-list');
        renderCardGrid(data.trending, 'foryou-trend-list');
        renderCardGrid(data.discovery, 'foryou-discovery-list');
    } catch(e) { console.error("ForYou Err", e); }
}

async function loadLikedSongs() {
    const container = document.getElementById('liked-songs-grid');
    if(!container) return;

    try {
        const res = await fetch(`${API}/favorites/user/${USER_ID}`);
        const songs = await res.json();

        document.getElementById('liked-count-text').innerText = `${songs.length} bài hát`;
        renderCardGrid(songs, 'liked-songs-grid');
    } catch(e) { console.error("Liked Err", e); }
}

// C. HOME & RANKING & HISTORY
async function loadHomeData() {
    const container = document.getElementById('recommend-list');
    if(container.children.length > 0) return;
    try {
        const res = await fetch(`${API}/dashboard/${USER_ID}`);
        const data = await res.json();
        document.getElementById('loading-rec').style.display = 'none';
        renderCardGrid(data.recommendedSongs, 'recommend-list');
        renderHorizontalList(data.newReleases, 'new-release-list');
        renderCardGrid(data.trendingSongs, 'trending-list');
    } catch(e) { console.error(e); }
}

async function loadRankingData() {
    const container = document.getElementById('ranking-list');
    container.innerHTML = '<div class="spinner-border text-info"></div>';
    document.querySelectorAll('.filter-btn').forEach(btn =>
        btn.classList.toggle('active', btn.getAttribute('onclick').includes(currentRankMode)));

    try {
        const res = await fetch(`${API}/ranking?mode=${currentRankMode}`);
        const data = await res.json();
        container.innerHTML = '';
        data.forEach((item, index) => {
            const s = item.song;
            const rank = index + 1;
            const color = rank===1?'text-info':rank===2?'text-success':rank===3?'text-warning':'text-white';
            container.innerHTML += `
                <div class="d-flex align-items-center bg-dark p-3 rounded mb-2 hover-bg-secondary group-action">
                    <div class="fw-bold fs-3 me-4 ${color}" style="min-width: 30px; text-align: center;">${rank}</div>
                    <div class="position-relative me-3" style="width:60px;height:60px">
                        <img src="${s.coverImage}" class="w-100 h-100 rounded object-fit-cover">
                        <div class="position-absolute top-50 start-50 translate-middle opacity-0 hover-visible cursor-pointer" onclick="playSongGlobal(event, ${s.songId})">
                             <i class="bi bi-play-circle-fill fs-2 text-white"></i>
                        </div>
                    </div>
                    <div class="flex-grow-1 cursor-pointer" onclick="navigateToSong(${s.songId})">
                        <div class="fw-bold text-white">${s.title}</div>
                        <small class="text-white-50">${s.artist?.name}</small>
                    </div>
                    <div class="text-end fw-bold text-info">${item.plays}</div>
                </div>`;
        });
    } catch(e) { console.error(e); }
}
function changeRankMode(mode) { currentRankMode = mode; loadRankingData(); }

async function loadHistoryData() {
    const tbody = document.getElementById('history-body');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center"><div class="spinner-border text-info"></div></td></tr>';
    const res = await fetch(`${API}/history/user/${USER_ID}`);
    const data = await res.json();
    document.getElementById('history-count').innerText = `(${data.length})`;
    tbody.innerHTML = '';
    data.forEach((h, idx) => {
        const s = h.song;
        tbody.innerHTML += `
        <tr class="group-action">
            <td class="text-white-50 text-center">${idx + 1}</td>
            <td>
                <div class="d-flex align-items-center">
                    <img src="${s.coverImage}" class="rounded me-3" width="40" height="40">
                    <div class="cursor-pointer" onclick="navigateToSong(${s.songId})">
                        <div class="fw-bold text-white text-truncate">${s.title}</div>
                    </div>
                </div>
            </td>
            <td class="text-white-50">${s.artist?.name}</td>
            <td class="text-white-50 small">${new Date(h.listenedAt).toLocaleDateString()}</td>
            <td class="text-end">
                <div class="dropdown">
                    <button class="btn btn-sm text-secondary" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></button>
                    <ul class="dropdown-menu dropdown-menu-dark">
                        <li><a class="dropdown-item text-danger" href="#" onclick="deleteHistoryItem(${h.historyId})">Xóa</a></li>
                        <li><a class="dropdown-item" href="#" onclick="openAddToPlaylistModal(${s.songId})">Thêm vào Playlist</a></li>
                    </ul>
                </div>
            </td>
        </tr>`;
    });
}
async function deleteHistoryItem(hid) {
    if(!confirm("Xóa dòng này?")) return;
    await fetch(`${API}/history/${hid}`, {method: 'DELETE'});
    loadHistoryData();
}

async function loadSidebarPlaylists() {
    const res = await fetch(`${API}/playlists/user/${USER_ID}`);
    const data = await res.json();
    sidebarPlaylistsData = data.sort((a,b)=>b.playlistId-a.playlistId);
    renderSidebarUI();
}
function renderSidebarUI() {
    const div = document.getElementById('sidebar-playlists');
    if(!div) return;
    const list = isSidebarExpanded ? sidebarPlaylistsData : sidebarPlaylistsData.slice(0, 5);
    div.innerHTML = list.map(p => `
        <div class="nav-link text-truncate text-white-50 cursor-pointer" onclick="switchView('playlist-detail', ${p.playlistId})">
            <i class="bi bi-music-note-list"></i> ${p.name}
        </div>`).join('');

    const btn = document.getElementById('btn-sidebar-expand');
    if(btn) {
        btn.classList.toggle('d-none', sidebarPlaylistsData.length <= 5);
        btn.innerHTML = isSidebarExpanded ? 'Thu gọn' : 'Xem thêm';
    }
}
function renderUserPlaylistsGrid() {
    const div = document.getElementById('user-playlists-grid');
    if(div) div.innerHTML = sidebarPlaylistsData.map(p => `
        <div class="col"><div class="custom-card" onclick="switchView('playlist-detail', ${p.playlistId})">
            <img src="${p.imageUrl||'https://placehold.co/300'}" class="w-100 rounded mb-2">
            <h6 class="text-white fw-bold">${p.name}</h6>
        </div></div>`).join('');
}


// D. SONG DETAIL SPA (FULL RESTORATION)
async function loadSongDetailSPA(sid) {
    const container = document.getElementById('view-song-detail');
    container.innerHTML = `<div class="d-flex justify-content-center align-items-center h-100"><div class="spinner-border text-info"></div></div>`;

    try {
        const res = await fetch(`${API}/songs/${sid}/detail?userId=${USER_ID}`);
        const data = await res.json();
        const s = data.song;

        // CSS Injection (Chỉ inject 1 lần nếu chưa có)
        if(!document.getElementById('song-detail-css')) {
            const style = document.createElement('style');
            style.id = 'song-detail-css';
            style.innerHTML = `
                .bg-blur { position: fixed; inset: 0; z-index: -1; background-size: cover; background-position: center; filter: blur(50px) brightness(0.4); }
                .hero-section { display: flex; gap: 30px; margin-top: 30px; align-items: flex-end; }
                .cd-thumb { width: 300px; height: 300px; border-radius: 8px; box-shadow: 0 8px 30px rgba(0,0,0,0.5); object-fit: cover; }
                .btn-play-lg { background: #00f3ff; color: #000; border: none; padding: 12px 35px; border-radius: 30px; font-weight: 700; text-transform: uppercase; }
                .btn-round { width: 45px; height: 45px; border-radius: 50%; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); color: white; display: inline-flex; align-items: center; justify-content: center; }
                .content-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 40px; margin-top: 50px; }
                .lyrics-box { background: rgba(255,255,255,0.05); padding: 30px; border-radius: 12px; backdrop-filter: blur(10px); }
                .suggestion-item { display: flex; align-items: center; padding: 10px; border-radius: 6px; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.05); }
                .suggestion-item:hover { background: rgba(255,255,255,0.1); }
            `;
            document.head.appendChild(style);
        }

        container.innerHTML = `
        <div class="position-relative w-100" style="min-height: 100vh; overflow-x: hidden; padding-bottom: 100px;">
            <div class="bg-blur" style="background-image: url('${s.coverImage}')"></div>
            
            <nav class="p-4 border-bottom border-white border-opacity-10">
                <div class="container d-flex justify-content-between">
                    <a href="javascript:void(0)" onclick="window.history.back()" class="text-decoration-none text-white fw-bold"><i class="bi bi-arrow-left me-2"></i>QUAY LẠI</a>
                </div>
            </nav>

            <div class="container main-container">
                <div class="hero-section">
                    <img src="${s.coverImage}" class="cd-thumb">
                    <div class="song-info flex-grow-1">
                        <h5 class="text-info text-uppercase letter-spacing-2">Bài Hát</h5>
                        <h1 class="display-3 fw-bold text-white">${s.title}</h1>
                        <div class="fs-5 text-white-50 mb-3">${s.artist?.name} • 2025</div>
                        
                        <div class="d-flex align-items-center gap-4 mb-4 text-secondary">
                            <div title="Lượt thích"><i class="bi bi-heart-fill me-2 text-danger"></i> <span class="text-white">${data.totalLikes}</span></div>
                            <div title="Lượt nghe"><i class="bi bi-headphones me-2"></i> <span class="text-white">${s.views || 0}</span></div>
                        </div>

                        <div class="d-flex align-items-center gap-2 mt-4">
                            <button class="btn-play-lg hover-scale" onclick="playSongGlobal(null, ${s.songId})"><i class="bi bi-play-circle-fill me-2"></i> PHÁT NGAY</button>
                            <button class="btn-round hover-scale" onclick="openAddToPlaylistModal(${s.songId})">
                                <i class="bi bi-plus-lg"></i>
                            </button>
                            <button class="btn-round hover-scale" onclick="alert('Tính năng tải xuống VIP')">
                                <i class="bi bi-download"></i>
                            </button>
                        </div>
                    </div>
                </div>

                <div class="content-grid">
                    <div class="left-col">
                        <div class="lyrics-box mb-5">
                            <h4 class="fw-bold mb-4 text-info"><i class="bi bi-mic-fill me-2"></i>Lời bài hát</h4>
                            <div class="text-white-50" style="white-space: pre-line; line-height: 2;">${s.lyrics || 'Đang cập nhật lời bài hát...'}</div>
                        </div>
                        <div class="comments-section">
                             <h4 class="fw-bold mb-3">Bình luận</h4>
                             <p class="text-muted">Tính năng bình luận đang được bảo trì.</p>
                        </div>
                    </div>

                    <div class="right-col">
                        <h5 class="fw-bold mb-3 text-uppercase small text-secondary">Có thể bạn thích</h5>
                        <div id="sd-recommend-list">
                            ${(data.relatedSongs||[]).map(r => `
                                <div class="suggestion-item" onclick="navigateToSong(${r.songId})">
                                    <img src="${r.coverImage}" class="rounded me-3" width="50" height="50" style="object-fit:cover">
                                    <div class="overflow-hidden flex-grow-1">
                                        <div class="fw-bold text-white text-truncate">${r.title}</div>
                                        <small class="text-white-50">${r.artist?.name}</small>
                                    </div>
                                    <i class="bi bi-play-fill fs-4 text-secondary"></i>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
    } catch(e) { container.innerHTML = `<div class="text-center pt-5 text-danger">Lỗi tải bài hát</div>`; }

    setTimeout(addDeepAnalysisButton, 100);
}


// E. ADD TO PLAYLIST (FIXED: CHECKBOX MODAL)
async function openAddToPlaylistModal(sid) {
    targetSongIdForAdd = sid;
    const modal = new bootstrap.Modal(document.getElementById('playlistModal'));

    // [FIX] Render dạng Checkbox List
    const res = await fetch(`${API}/playlists/user/${USER_ID}`);
    const playlists = await res.json();
    const div = document.getElementById('playlist-opts');

    div.innerHTML = `<div class="d-flex flex-column gap-2">`;
    if(playlists.length === 0) div.innerHTML += `<div class="text-muted text-center">Bạn chưa có playlist nào</div>`;

    playlists.forEach(p => {
        div.innerHTML += `
            <div class="form-check bg-dark border border-secondary p-3 rounded d-flex align-items-center">
                <input class="form-check-input me-3" type="checkbox" value="${p.playlistId}" id="chk-pl-${p.playlistId}" style="transform: scale(1.3);">
                <label class="form-check-label text-white fw-bold flex-grow-1 cursor-pointer" for="chk-pl-${p.playlistId}">
                    ${p.name}
                    <div class="small text-white-50">${p.isPublic ? 'Công khai' : 'Riêng tư'}</div>
                </label>
            </div>`;
    });
    div.innerHTML += `</div>
        <div class="mt-3 text-end border-top border-secondary pt-3">
            <button class="btn btn-success rounded-pill px-4 fw-bold" onclick="confirmAddSongs()">Xác nhận</button>
        </div>`;

    modal.show();
}

async function confirmAddSongs() {
    const checkboxes = document.querySelectorAll('input[id^="chk-pl-"]:checked');
    if(checkboxes.length === 0) return alert("Vui lòng chọn ít nhất 1 playlist");

    const promises = Array.from(checkboxes).map(chk => {
        const pid = chk.value;
        return fetch(`${API}/playlists/${pid}/songs`, {
            method: 'POST', headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({songId: targetSongIdForAdd})
        });
    });

    await Promise.all(promises);
    alert("Đã thêm vào các playlist đã chọn!");
    bootstrap.Modal.getInstance(document.getElementById('playlistModal')).hide();
}


// --- UTILS ---
function navigateToSong(sid) { switchView('song-detail', sid); }
function playSongGlobal(e, sid) {
    if(e) e.stopPropagation();
    fetch(`${API}/history?userId=${USER_ID}&songId=${sid}`, {method:'POST'});
    // Fake Player Logic
    fetch(`${API}/songs/${sid}/detail`).then(r=>r.json()).then(res=>{
        const s = res.song;
        document.getElementById('player-title').innerText = s.title;
        document.getElementById('player-artist').innerText = s.artist?.name;
        document.getElementById('player-img').src = s.coverImage;
        const audio = document.getElementById('main-audio');
        if(audio && s.filePath) { audio.src=s.filePath; audio.play(); }
    });
}

function renderCardGrid(list, id) {
    const div = document.getElementById(id);
    if(div) div.innerHTML = (list||[]).map(s => `
        <div class="col"><div class="custom-card position-relative group-action">
            <div class="position-relative mb-2">
                <img src="${s.coverImage}" class="w-100 rounded shadow-sm">
                <div class="position-absolute top-50 start-50 translate-middle opacity-0 hover-visible cursor-pointer" onclick="playSongGlobal(event, ${s.songId})"><i class="bi bi-play-circle-fill text-success fs-1"></i></div>
            </div>
            <div onclick="navigateToSong(${s.songId})" class="cursor-pointer"><h6 class="text-white text-truncate fw-bold">${s.title}</h6><small class="text-white-50">${s.artist?.name}</small></div>
        </div></div>`).join('');
}

// Trong renderCardGrid hoặc hàm hiển thị bài hát
function renderCardWithDetails(song) {
    let html = `
        <div class="custom-card position-relative">
            <div class="position-relative mb-2">
                <img src="${song.coverImage}" class="w-100 rounded shadow-sm">
                <div class="position-absolute top-50 start-50 translate-middle opacity-0 hover-visible cursor-pointer" 
                     onclick="playSongGlobal(event, ${song.songId})">
                    <i class="bi bi-play-circle-fill text-success fs-1"></i>
                </div>
                <div class="position-absolute bottom-0 end-0 m-2">
                    ${song.explicit ? '<span class="badge bg-danger">E</span>' : ''}
                    ${song.isVip ? '<span class="badge bg-warning ms-1">VIP</span>' : ''}
                </div>
            </div>
            
            <div onclick="navigateToSong(${song.songId})" class="cursor-pointer">
                <h6 class="text-white text-truncate fw-bold">${song.title}</h6>
                <small class="text-white-50">${song.artist?.name || song.artistName}</small>
                
                <div class="mt-2 d-flex justify-content-between align-items-center">
                    <small class="text-info">${song.formattedDuration || '0:00'}</small>
                    ${song.averageRating ?
        `<small class="text-warning">
                            <i class="bi bi-star-fill"></i> ${song.averageRating.toFixed(1)}
                        </small>` : ''
    }
                </div>
                
                <div class="mt-1 d-flex gap-2">
                    ${song.bpm ? `<span class="badge bg-dark">${song.bpm} BPM</span>` : ''}
                    ${song.language ? `<span class="badge bg-secondary">${song.language.toUpperCase()}</span>` : ''}
                    ${song.releaseYear ? `<span class="badge bg-info">${song.releaseYear}</span>` : ''}
                </div>
            </div>
        </div>
    `;
    return html;
}

function renderHorizontalList(list, id) {
    const div = document.getElementById(id);
    if(div) div.innerHTML = (list||[]).map(s => `
        <div class="col-12 col-md-6 col-lg-4">
            <div class="d-flex align-items-center bg-dark p-2 rounded border border-secondary border-opacity-25 hover-bg-secondary transition-fast">
                <img src="${s.coverImage}" class="rounded me-3" width="60" height="60">
                <div class="flex-grow-1 overflow-hidden cursor-pointer" onclick="navigateToSong(${s.songId})">
                    <h6 class="mb-0 text-white fw-bold text-truncate">${s.title}</h6><small class="text-muted">${s.artist?.name}</small>
                </div>
                <i class="bi bi-play-circle-fill fs-3 text-white cursor-pointer ms-2" onclick="playSongGlobal(event, ${s.songId})"></i>
            </div>
        </div>`).join('');
}

// AI DJ FUNCTIONS

// 1. AI DJ ĐƠN GIẢN (Theo tâm trạng)
function openAiDjModal() {
    const modalEl = document.getElementById('aiDjModal');
    if (modalEl) {
        document.getElementById('ai-mood-input').value = ''; // Reset input
        document.getElementById('ai-dj-loading').classList.add('d-none');
        new bootstrap.Modal(modalEl).show();
    }
}

async function submitAiDj() {
    const mood = document.getElementById('ai-mood-input').value.trim();
    if (!mood) return showToast('⚠️ Hãy nhập cảm xúc của bạn!', 'warning');

    const btn = document.querySelector('#aiDjModal .btn-info');
    const loading = document.getElementById('ai-dj-loading');

    // UI Loading
    btn.disabled = true;
    loading.classList.remove('d-none');

    try {
        const res = await fetch(`${API}/gemini/recommend-by-mood`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mood: mood }) // Backend mới dùng endpoint này
        });

        if (!res.ok) throw new Error('API Error');
        const songs = await res.json();

        // Xử lý kết quả
        bootstrap.Modal.getInstance(document.getElementById('aiDjModal')).hide();
        displayAiResults(`Cảm xúc: "${mood}"`, songs);
        showToast('✨ AI đã tạo playlist cho bạn!', 'success');

    } catch (e) {
        console.error(e);
        showToast('❌ AI đang bận, thử lại sau nhé!', 'danger');
    } finally {
        btn.disabled = false;
        loading.classList.add('d-none');
    }
}

// 2. AI NÂNG CAO (Form chi tiết)
function openAdvancedAiModal() {
    const modalEl = document.getElementById('advancedAiModal');
    if (modalEl) {
        document.getElementById('advanced-ai-form').reset(); // Reset form
        new bootstrap.Modal(modalEl).show();
    }
}

async function submitAdvancedAi() {
    const form = document.getElementById('advanced-ai-form');
    const formData = new FormData(form);

    // Thu thập dữ liệu
    const request = {
        description: formData.get('description'),
        moods: formData.getAll('moods'),
        genres: formData.getAll('genres'),
        activity: formData.get('activity'),
        timeOfDay: formData.get('timeOfDay'),
        duration: parseInt(formData.get('duration')) || 60,
        songCount: parseInt(formData.get('songCount')) || 15,
        minRating: parseFloat(formData.get('minRating')) || 3.5,
        minYear: parseInt(formData.get('minYear')) || 2000,
        excludeListened: formData.get('excludeListened') === 'on'
    };

    // UI Loading
    const btn = document.querySelector('#advancedAiModal .btn-info');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang phân tích...';

    try {
        const res = await fetch(`${API}/gemini/advanced-recommend`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });

        if (!res.ok) throw new Error('API Error');
        const songs = await res.json();

        // Xử lý kết quả
        bootstrap.Modal.getInstance(document.getElementById('advancedAiModal')).hide();

        // Tạo mô tả ngắn gọn cho kết quả
        const summary = `Nâng cao: ${request.description || request.moods.join(', ') || 'Theo yêu cầu'}`;
        displayAiResults(summary, songs);

    } catch (e) {
        console.error(e);
        showToast('❌ Lỗi xử lý yêu cầu', 'danger');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

// Helper: Hiển thị kết quả AI ra màn hình
function displayAiResults(title, songs) {
    switchView('ai-result');

    const displayEl = document.getElementById('ai-user-mood-display');
    if(displayEl) {
        displayEl.innerHTML = `
            <div class="alert alert-info border-info bg-dark">
                <i class="bi bi-robot me-2"></i><strong>Yêu cầu:</strong> ${title}
                <br><small class="ms-4 text-white-50">AI tìm thấy ${songs.length} bài hát phù hợp</small>
            </div>`;
    }

    renderCardGrid(songs, 'ai-result-list');
}

// 3. PHÂN TÍCH GU ÂM NHẠC (Analyze Habits)
async function analyzeMyHabits() {
    showToast('🤖 AI đang đọc lịch sử nghe nhạc của bạn...', 'info');

    try {
        const res = await fetch(`${API}/gemini/analyze-listening-habits/${USER_ID}`, {
            method: 'POST' // Backend dùng POST
        });
        const analysisMarkdown = await res.text();

        // Hiển thị Modal kết quả
        showComparisonModal(analysisMarkdown, 'Phân Tích Gu Âm Nhạc');

    } catch (e) {
        showToast('❌ Chưa đủ dữ liệu lịch sử để phân tích', 'warning');
    }
}

// ============================================
// 1. AI GIẢI NGHĨA BÀI HÁT (Single Song Analysis)
// ============================================

async function askAiAboutSong(sid) {
    const modal = new bootstrap.Modal(document.getElementById('aiModal'));
    const contentDiv = document.getElementById('ai-response-content');

    contentDiv.innerHTML = `
        <div class="text-center py-4">
            <div class="spinner-border text-info" role="status"></div>
            <p class="mt-2 text-white-50">AI đang nghe và phân tích bài hát...</p>
            <small class="text-muted">Gemini AI đang xử lý</small>
        </div>
    `;
    modal.show();

    try {
        const res = await fetch(`${API}/gemini/explain-song/${sid}`);

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const text = await res.text();

        // Hiệu ứng gõ chữ
        contentDiv.innerHTML = '<div class="ai-typing-text"></div>';
        const typingDiv = contentDiv.querySelector('.ai-typing-text');

        let i = 0;
        function typeWriter() {
            if (i < text.length) {
                typingDiv.innerHTML += text.charAt(i);
                i++;
                // Auto scroll
                contentDiv.scrollTop = contentDiv.scrollHeight;
                setTimeout(typeWriter, 15);
            } else {
                // Thêm nút copy khi hoàn thành
                contentDiv.innerHTML += `
                    <div class="mt-3 text-end">
                        <button class="btn btn-sm btn-outline-info" onclick="copyToClipboard('${text.replace(/'/g, "\\'")}')">
                            <i class="bi bi-clipboard"></i> Sao chép
                        </button>
                    </div>
                `;
            }
        }
        typeWriter();

    } catch (e) {
        contentDiv.innerHTML = `
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle"></i>
                <strong>Lỗi kết nối AI:</strong> ${e.message}
                <br><small>Vui lòng thử lại sau</small>
            </div>
        `;
    }
}


// 4. SO SÁNH BÀI HÁT (Compare)
async function compareMultipleSongs() {
    // Logic lấy danh sách bài hát đã chọn (cần checkbox trong giao diện)
    const checkboxes = document.querySelectorAll('input[name="song-select"]:checked');
    const songIds = Array.from(checkboxes).map(cb => parseInt(cb.value));

    if (songIds.length < 2) return showToast('⚠️ Chọn ít nhất 2 bài để so sánh', 'warning');
    if (songIds.length > 5) return showToast('⚠️ Tối đa 5 bài thôi nhé', 'warning');

    showToast('⚖️ AI đang so sánh...', 'info');

    try {
        const res = await fetch(`${API}/gemini/compare-songs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ songIds: songIds, userId: USER_ID })
        });
        const resultMarkdown = await res.text();
        showComparisonModal(resultMarkdown, 'So Sánh Bài Hát');
    } catch (e) {
        showToast('❌ Lỗi so sánh', 'danger');
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

// Toast Notification System
function showToast(message, type = 'info') {
    const toastContainer = document.querySelector('.toast-container') || createToastContainer();

    const bgColors = {
        'success': 'bg-success',
        'danger': 'bg-danger',
        'warning': 'bg-warning',
        'info': 'bg-info'
    };

    const toastHTML = `
        <div class="toast align-items-center text-white ${bgColors[type]} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body fw-bold">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;

    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    const toastEl = toastContainer.lastElementChild;
    const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
    toast.show();

    // Auto remove after hide
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    container.style.zIndex = '9999';
    document.body.appendChild(container);
    return container;
}

// Export Playlist (Lưu kết quả AI thành Playlist thật)
async function saveAsPlaylist() {
    const playlistName = prompt('Tên playlist:', 'AI Playlist - ' + new Date().toLocaleDateString());
    if (!playlistName) return;

    try {
        // Tạo playlist mới
        const createRes = await fetch(`${API}/playlists`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: USER_ID,
                name: playlistName,
                description: 'Playlist được tạo bởi AI',
                isPublic: false
            })
        });

        const newPlaylist = await createRes.json();
        const playlistId = newPlaylist.playlistId;

        // Lấy tất cả bài hát trong kết quả AI
        const songCards = document.querySelectorAll('#ai-result-list .custom-card');
        const promises = [];

        songCards.forEach((card, index) => {
            // Extract song ID from onclick attribute
            const onclickAttr = card.querySelector('[onclick*="navigateToSong"]')?.getAttribute('onclick');
            const match = onclickAttr?.match(/navigateToSong\((\d+)\)/);

            if (match) {
                const songId = match[1];
                promises.push(
                    fetch(`${API}/playlists/${playlistId}/songs`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ songId: parseInt(songId) })
                    })
                );
            }
        });

        await Promise.all(promises);

        showToast(`✅ Đã lưu ${promises.length} bài hát vào playlist "${playlistName}"`, 'success');

        // Reload sidebar
        loadSidebarPlaylists();

        // Hỏi có muốn xem playlist không
        if (confirm('Bạn có muốn xem playlist vừa tạo không?')) {
            switchView('playlist-detail', playlistId);
        }

    } catch (error) {
        console.error('Save playlist error:', error);
        showToast('❌ Lỗi khi lưu playlist', 'danger');
    }
}

// Export danh sách bài hát ra file
function exportPlaylist() {
    const songCards = document.querySelectorAll('#ai-result-list .custom-card');
    let csvContent = "STT,Tên bài hát,Nghệ sĩ\n";

    songCards.forEach((card, index) => {
        const title = card.querySelector('h6')?.innerText || 'Unknown';
        const artist = card.querySelector('small')?.innerText || 'Unknown';
        csvContent += `${index + 1},"${title}","${artist}"\n`;
    });

    // Download CSV
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `AI_Playlist_${new Date().getTime()}.csv`;
    link.click();

    showToast('📥 Đã xuất danh sách bài hát', 'success');
}

// ============================================
// AI CHAT ASSISTANT (Bonus Feature)
// ============================================

// 5. CHAT ASSISTANT
let chatHistory = [];
function openAiChatAssistant() {
    chatHistory = []; // Reset history mới
    const modalEl = document.getElementById('aiChatModal'); // Đảm bảo HTML có modal này (như file cũ)
    if(modalEl) new bootstrap.Modal(modalEl).show();
}

async function sendChatMessage() {
    const input = document.getElementById('chatInput');
    const msg = input.value.trim();
    if(!msg) return;

    // Render User Msg
    const box = document.getElementById('chatMessages');
    box.innerHTML += `<div class="text-end mb-2"><span class="bg-info text-dark p-2 rounded">${msg}</span></div>`;
    input.value = '';

    // Call API
    try {
        const res = await fetch(`${API}/gemini/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: msg, history: chatHistory, userId: USER_ID })
        });
        const aiReply = await res.text();

        // Render AI Msg
        box.innerHTML += `<div class="text-start mb-2"><span class="bg-secondary p-2 rounded">${aiReply}</span></div>`;
        box.scrollTop = box.scrollHeight;

        // Save History
        chatHistory.push({role: 'user', content: msg});
        chatHistory.push({role: 'model', content: aiReply});
    } catch(e) {
        box.innerHTML += `<div class="text-center text-danger small">Lỗi kết nối</div>`;
    }
}

// 6. HELPER: Hiển thị Modal chứa Markdown (Dùng chung cho Compare & Habits)
function showComparisonModal(markdownText, title = 'Kết quả AI') {
    let modalEl = document.getElementById('comparisonModal');
    if (!modalEl) return; // Đảm bảo HTML đã có modal này

    // Cập nhật title
    modalEl.querySelector('.modal-title').innerHTML = `<i class="bi bi-stars me-2"></i>${title}`;

    // Convert Markdown đơn giản sang HTML (nếu ko có thư viện marked)
    let html = markdownText
        .replace(/\*\*(.*?)\*\*/g, '<strong class="text-info">$1</strong>') // Bold
        .replace(/\n/g, '<br>'); // Newline

    document.getElementById('comparisonContent').innerHTML = `<div class="p-3 bg-black bg-opacity-25 rounded lh-lg">${html}</div>`;

    new bootstrap.Modal(modalEl).show();
}

// ============================================
// GLOBAL EXPORTS (để có thể gọi từ HTML)
// ============================================

window.askAiAboutSong = askAiAboutSong;
window.openAiDjModal = openAiDjModal;
window.submitAiDj = submitAiDj;
window.openAdvancedAiModal = openAdvancedAiModal;
window.submitAdvancedAi = submitAdvancedAi;
window.openDeepAnalysisModal = openDeepAnalysisModal;
window.startDeepAnalysis = startDeepAnalysis;
window.saveAsPlaylist = saveAsPlaylist;
window.exportPlaylist = exportPlaylist;
window.compareMultipleSongs = compareMultipleSongs;
window.openAiChatAssistant = openAiChatAssistant;
window.sendChatMessage = sendChatMessage;
window.copyToClipboard = copyToClipboard;
window.copyAnalysisToClipboard = copyAnalysisToClipboard;
window.exportAnalysisAsPDF = exportAnalysisAsPDF;