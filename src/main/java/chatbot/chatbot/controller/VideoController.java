package chatbot.chatbot.controller;

import chatbot.chatbot.dto.SearchRequest;
import chatbot.chatbot.model.Video;
import chatbot.chatbot.service.YoutubeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/video")
public class VideoController {

    private final YoutubeService youtubeService;

    public VideoController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    // =========================
    // 1️⃣ ENTRY FROM HOME
    // URL → /video
    // =========================
    @GetMapping
    public String videoHome(Model model) {
        model.addAttribute("searchRequest", new SearchRequest());
        return "video-search";
    }

    // =========================
    // 2️⃣ SEARCH VIDEOS
    // URL → /video/search
    // =========================
    @PostMapping("/search")
    public String searchVideos(@ModelAttribute SearchRequest request,
                               Model model) throws Exception {

        model.addAttribute("videos",
                youtubeService.searchVideos(request));

        return "video-results";
    }

    // SAFE GET (refresh protection)
    @GetMapping("/search")
    public String searchRedirect() {
        return "redirect:/video";
    }

    // =========================
    // 3️⃣ WATCH VIDEO
    // URL → /video/watch/{youtubeId}
    // =========================
    @GetMapping("/watch/{youtubeId}")
    public String watchVideo(@PathVariable String youtubeId,
                             Model model) throws Exception {

        model.addAttribute("video",
                youtubeService.getVideoById(youtubeId));

        return "video-watch";
    }

    // =========================
    // 4️⃣ ADD FAVORITE
    // URL → /video/favorite
    // =========================
    @PostMapping("/favorite")
    public String addFavorite(@RequestParam String youtubeId,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam String thumbnailUrl,
                              @RequestParam String category) {

        Video video = new Video(
                youtubeId,
                title,
                description,
                thumbnailUrl,
                category
        );
        video.setFavorite(true);

        youtubeService.saveVideo(video);

        // ✅ REDIRECT MUST MATCH MAPPING
        return "redirect:/video/favorites";
    }

    // =========================
    // 5️⃣ FAVORITES PAGE
    // URL → /video/favorites
    // =========================
    @GetMapping("/favorites")
    public String favorites(Model model) {

        model.addAttribute("videos",
                youtubeService.getFavoriteVideos());

        // ✅ FILE NAME = video-Favorites.html
        return "video-Favorites";
    }

    // =========================
    // 6️⃣ DELETE FAVORITE
    // URL → /video/favorites/delete/{id}
    // =========================
    @PostMapping("/favorites/delete/{id}")
    public String deleteFavorite(@PathVariable Long id) {

        youtubeService.deleteVideo(id);
        return "redirect:/video/favorites";
    }
}
