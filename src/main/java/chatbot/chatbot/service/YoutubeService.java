package chatbot.chatbot.service;

import chatbot.chatbot.dto.SearchRequest;
import chatbot.chatbot.model.Video;
import chatbot.chatbot.repo.VideoRepository;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.ThumbnailDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class YoutubeService {

    private static final String APPLICATION_NAME = "Video Learning";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static YouTube youtube;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Autowired
    private VideoRepository videoRepository;

    // 🔍 SEARCH YOUTUBE VIDEOS
    public List<Video> searchVideos(SearchRequest request) throws Exception {

        YouTube yt = getYouTubeService();

        YouTube.Search.List search = yt.search().list("snippet");
        search.setKey(apiKey);
        search.setQ(request.getQuery() + " tutorial");
        search.setType("video");
        search.setMaxResults(request.getMaxResults().longValue());

        List<SearchResult> results = search.execute().getItems();
        List<Video> videos = new ArrayList<>();

        for (SearchResult r : results) {
            ThumbnailDetails t = r.getSnippet().getThumbnails();

            String thumbnailUrl =
                    t.getHigh() != null ? t.getHigh().getUrl() :
                            t.getMedium() != null ? t.getMedium().getUrl() :
                                    t.getDefault().getUrl();

            videos.add(new Video(
                    r.getId().getVideoId(),
                    r.getSnippet().getTitle(),
                    r.getSnippet().getDescription(),
                    thumbnailUrl,
                    "Learning"
            ));
        }

        return videos;
    }

    // ▶ GET SINGLE VIDEO DETAILS
    public Video getVideoById(String id) throws Exception {

        YouTube yt = getYouTubeService();

        YouTube.Videos.List v = yt.videos().list("snippet");
        v.setKey(apiKey);
        v.setId(id);

        var ytVideo = v.execute().getItems().get(0);

        return new Video(
                ytVideo.getId(),
                ytVideo.getSnippet().getTitle(),
                ytVideo.getSnippet().getDescription(),
                ytVideo.getSnippet().getThumbnails().getHigh().getUrl(),
                "Learning"
        );
    }

    // ⭐ SAVE FAVORITE VIDEO
    public Video saveVideo(Video video) {
        video.setFavorite(true);
        return videoRepository.save(video);
    }

    // ⭐ GET ALL FAVORITES
    public List<Video> getFavoriteVideos() {
        return videoRepository.findByFavoriteTrue();
    }

    // ❌ DELETE FAVORITE
    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

    // 🔧 YOUTUBE CLIENT
    private YouTube getYouTubeService() throws Exception {
        if (youtube == null) {
            youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    null
            ).setApplicationName(APPLICATION_NAME).build();
        }
        return youtube;
    }
}
