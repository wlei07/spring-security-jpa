package io.leiwang.springsecurityjpa.posts;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "http://localhost:4200")
public class PostController {
    private static final Map<Integer, Post> posts = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        posts.put(1, new Post(1, "title1"));
        posts.put(2, new Post(2, "title2"));
        posts.put(3, new Post(3, "title3"));
        posts.put(4, new Post(4, "title4"));
        posts.put(5, new Post(5, "title5"));
    }

    @GetMapping
    public List<Post> getPosts() {
        return posts.values().stream().toList();
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        if (posts.values().stream().map(Post::title).collect(Collectors.toSet()).contains(post.title())) {
            throw new ResponseStatusException(BAD_REQUEST, "Post is duplicated.");
        }
        int postId = posts.keySet().stream().max(Integer::compare).orElse(1);
        Post postToBeSaved = new Post(postId, post.title());
        posts.put(postId, postToBeSaved);
        return postToBeSaved;
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable int postId) {
        if (!posts.containsKey(postId)) {
            throw new ResponseStatusException(NOT_FOUND, "Post does not exist.");
        }
        posts.remove(postId);
    }

    @PatchMapping("/{postId}")
    public void updatePost(@PathVariable int postId, @RequestBody Post post) {
        if (!posts.containsKey(postId)) {
            throw new ResponseStatusException(NOT_FOUND, "Post does not exist.");
        }
        posts.put(postId, post);
    }
}
