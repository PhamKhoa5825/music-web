// global-player.js
// Đặt file này và include trong tất cả các trang

class GlobalPlayer {
    constructor() {
        this.currentSong = null;
        this.isPlaying = false;
        this.audio = null;
        this.initialize();
    }

    initialize() {
        // Create audio element if not exists
        if (!document.getElementById('global-audio')) {
            this.audio = document.createElement('audio');
            this.audio.id = 'global-audio';
            this.audio.crossOrigin = 'anonymous';
            document.body.appendChild(this.audio);
        } else {
            this.audio = document.getElementById('global-audio');
        }

        // Setup event listeners
        this.setupEventListeners();
    }

    setupEventListeners() {
        this.audio.addEventListener('play', () => this.onPlay());
        this.audio.addEventListener('pause', () => this.onPause());
        this.audio.addEventListener('ended', () => this.onEnded());
        this.audio.addEventListener('timeupdate', () => this.onTimeUpdate());
    }

    async playSong(songId) {
        try {
            const res = await fetch(`/api/songs/${songId}/detail`);
            const data = await res.json();
            const song = data.song;

            this.currentSong = song;

            // Update player UI
            this.updatePlayerUI(song);

            // Play audio if available
            if (song.filePath) {
                this.audio.src = song.filePath;
                this.audio.play();
                this.isPlaying = true;
            }

            // Record in history
            const USER_ID = localStorage.getItem('music_user_id') || 1;
            await fetch(`/api/history?userId=${USER_ID}&songId=${songId}`, {
                method: 'POST'
            });

        } catch (error) {
            console.error('Error playing song:', error);
        }
    }

    updatePlayerUI(song) {
        // Update player elements if they exist
        const playerTitle = document.getElementById('player-title');
        const playerArtist = document.getElementById('player-artist');
        const playerImg = document.getElementById('player-img');
        const playBtn = document.getElementById('player-play-btn');

        if (playerTitle) playerTitle.textContent = song.title;
        if (playerArtist) playerArtist.textContent = song.artist?.name || 'Unknown';
        if (playerImg) playerImg.src = song.coverImage || 'https://placehold.co/60';
        if (playBtn) playBtn.className = 'bi bi-pause-circle-fill fs-1 text-white hover-scale cursor-pointer';
    }

    togglePlay() {
        if (this.isPlaying) {
            this.audio.pause();
        } else {
            this.audio.play();
        }
    }

    onPlay() {
        this.isPlaying = true;
        const playBtn = document.getElementById('player-play-btn');
        if (playBtn) playBtn.className = 'bi bi-pause-circle-fill fs-1 text-white hover-scale cursor-pointer';
    }

    onPause() {
        this.isPlaying = false;
        const playBtn = document.getElementById('player-play-btn');
        if (playBtn) playBtn.className = 'bi bi-play-circle-fill fs-1 text-white hover-scale cursor-pointer';
    }

    onEnded() {
        // Auto play next song logic here
    }

    onTimeUpdate() {
        const progress = document.getElementById('player-progress');
        if (progress && this.audio.duration) {
            const percentage = (this.audio.currentTime / this.audio.duration) * 100;
            progress.value = percentage;
        }
    }
}

// Initialize global player
window.globalPlayer = new GlobalPlayer();

// Helper function to play song from anywhere
window.playSong = function(songId) {
    window.globalPlayer.playSong(songId);
};