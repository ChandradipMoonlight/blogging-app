package com.blog.app.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.blog.app.builder.MessageProperties;
import com.blog.app.config.AppConstants;
import com.blog.app.dto.PostDTO;
import com.blog.app.dto.PostInputDTO;
import com.blog.app.dto.PostResponse;
import com.blog.app.dto.ResponseDTO;
import com.blog.app.service.IFileService;
import com.blog.app.service.IPostService;

@RestController
@RequestMapping("/api")
public class PostController {

	@Autowired
	private IPostService postService;

	@Autowired
	private IFileService fileService;

	@Value("${project.image}")
	private String path;

	@PostMapping("/user/{userId}/category/{categoryId}/posts")
	public ResponseEntity<ResponseDTO> createPost(@Valid @RequestBody PostInputDTO postInputDTO,
			@PathVariable("userId") Integer userId, @PathVariable("categoryId") Integer categoryId) {
		ResponseDTO response = new ResponseDTO(MessageProperties.POST_CREATED.getMessage(),
				postService.createPost(postInputDTO, userId, categoryId));
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/user/{postId}/posts")
	public ResponseEntity<ResponseDTO> updatePost(@PathVariable("postId") Integer postId,
			@Valid @RequestBody PostInputDTO postInputDTO) {
		ResponseDTO response = new ResponseDTO(MessageProperties.POST_UPDATED.getMessage(),
				postService.updatePost(postId, postInputDTO));
		return new ResponseEntity<ResponseDTO>(response, HttpStatus.OK);
	}

	@DeleteMapping("/posts/{postId}")
	public ResponseEntity<String> deletePost(@PathVariable("postId") Integer postId) {
		return new ResponseEntity<>(postService.deletePost(postId), HttpStatus.OK);
	}

	@GetMapping("/posts")
	public ResponseEntity<ResponseDTO> getAllPosts(
			@RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
			@RequestParam(value = "orderBy", defaultValue = AppConstants.ORDER_BY, required = false) String orderBy) {
		PostResponse postResponse = postService.getAllPosts(pageNumber, pageSize, sortBy, orderBy);
		ResponseDTO response = new ResponseDTO(MessageProperties.FETCHED_ALL_POSTS.getMessage(), postResponse);
		return new ResponseEntity<ResponseDTO>(response, HttpStatus.OK);
	}

	@GetMapping("/{postId}/post")
	public ResponseEntity<ResponseDTO> getPostById(@PathVariable("postId") Integer postId) {
		ResponseDTO response = new ResponseDTO(MessageProperties.FETCHED_POST.getMessage(),
				postService.getPostById(postId));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/category/{categoryId}/posts")
	public ResponseEntity<ResponseDTO> getAllPostsByCategory(@PathVariable("categoryId") Integer categoryId) {
		List<PostDTO> postList = postService.getAllPostsByCategory(categoryId);
		ResponseDTO response = new ResponseDTO(MessageProperties.FETCHED_ALL_POSTS.getMessage(), postList);
		return new ResponseEntity<ResponseDTO>(response, HttpStatus.OK);
	}

	@GetMapping("/user/{userId}/posts")
	public ResponseEntity<ResponseDTO> getAllPostByUser(@PathVariable("userId") Integer userId) {
		List<PostDTO> postList = postService.getAllPostsByUser(userId);
		ResponseDTO response = new ResponseDTO(MessageProperties.FETCHED_ALL_POSTS.getMessage(), postList);
		return new ResponseEntity<ResponseDTO>(response, HttpStatus.OK);
	}

	@GetMapping("/posts/search/{keywords}")
	public ResponseEntity<ResponseDTO> searchPostByTitle(@PathVariable("keywords") String keywords) {
		List<PostDTO> postsList = postService.searchPostByTitle(keywords);
		ResponseDTO response = new ResponseDTO(MessageProperties.FETCHED_ALL_POSTS.getMessage(), postsList);
		return new ResponseEntity<ResponseDTO>(response, HttpStatus.OK);
	}

	@PostMapping("/post/image/upload/{postId}")
	public ResponseEntity<ResponseDTO> uploadPostImage(@PathVariable("postId") Integer postId,
			@RequestParam MultipartFile image) throws IOException {
		PostDTO postDTO = postService.getPostById(postId);
		String imgName = fileService.uploadImage(path, image);
		postDTO.setImageName(imgName);
		ResponseDTO response = new ResponseDTO(MessageProperties.IMAGE_UPLOADED.getMessage(),
				postService.updatePost(postId, postDTO));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping(value = "post/img/{imgName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public void downloadImg(
			@PathVariable("imgName") String imgName,
			HttpServletResponse response
			) throws IOException {
		InputStream resource = fileService.getResource(path, imgName);
		StreamUtils.copy(resource, response.getOutputStream());
	}
 
}
