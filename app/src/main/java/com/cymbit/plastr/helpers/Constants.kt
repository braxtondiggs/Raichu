package com.cymbit.plastr.helpers

class Constants {
    companion object {
        val DEFAULT_SUBS= listOf("EarthPorn", "SpacePorn", "APodStream", "WindowShots", "Wallpapers", "ITookAPicture", "iWallpaper", "VerticalWallpapers", "MobileWallpaper", "AlbumArtPorn", "MusicWallpapers", "ConcertPorn", "ExposurePorn", "SkyPorn", "FractalPorn", "ImaginaryTechnology", "BridgePorn", "RedWall") as Collection<String>
        val SELECTED_DEFAULT_SUBS = listOf("iWallpaper", "SpacePorn", "ExposurePorn", "Wallpapers", "VerticalWallpapers", "MobileWallpaper") as Collection<String>
        val VALID_DOMAINS = listOf("500px.com", "abload.de", "deviantart.com", "deviantart.net", "fav.me", "fbcdn.net", "flickr.com", "gyazo.com", "imageshack.us" , "imgclean.com", "imgur.com", "instagr.am", "instagram.com", "mediacru.sh", "media.tumblr.com", "min.us", "minus.com", "myimghost.com", "photobucket.com", "picsarus.com", "puu.sh", "staticflickr.com", "tinypic.com", "twitpic.com", "redd.it", "apod.nasa.gov") as Collection<String>
        val FREQUENCY  = listOf("Off", "30 minutes", "1 hour", " 3 hours", " 6 hours", "9 hours", "12 hours", "18 hours", "1 day")
        val FREQUENCY_NUMBERS: List<Long> = listOf(0L, 30L, 60L, 180L, 360L, 540L, 720L, 1080L, 1440L)
        val NETWORK = listOf("Wi-Fi & Mobile Data", "Wi-Fi Only")
    }
}