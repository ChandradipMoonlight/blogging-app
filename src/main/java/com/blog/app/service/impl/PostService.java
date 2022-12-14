package com.blog.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.blog.app.builder.MessageProperties;
import com.blog.app.dto.PostDTO;
import com.blog.app.dto.PostInputDTO;
import com.blog.app.dto.PostResponse;
import com.blog.app.enitity.Category;
import com.blog.app.enitity.Post;
import com.blog.app.enitity.User;
import com.blog.app.exception.ResourceNotFoundException;
import com.blog.app.repositories.CategoryRepo;
import com.blog.app.repositories.PostRepo;
import com.blog.app.repositories.UserRepo;
import com.blog.app.service.IPostService;

@Service
public class PostService implements IPostService {

	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private CategoryRepo categoryRepo;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Override
	public PostDTO createPost(PostInputDTO postInputDTO, Integer userId, Integer categoryId) {
		User user = userRepo.findById(userId)
				.orElseThrow(()->new ResourceNotFoundException("User", "UserId", userId));
		
		Category category = categoryRepo.findById(categoryId)
				.orElseThrow(()-> new ResourceNotFoundException("Category", "CategoryId", categoryId));
		
		Post post = modelMapper.map(postInputDTO, Post.class);
		post.setCategory(category);
		post.setUser(user);
		postRepo.save(post);
		
		return modelMapper.map(post, PostDTO.class);
	}

	@Override
	public PostDTO updatePost(Integer postId, PostInputDTO postInputDTO) {
		Post post = postRepo.findById(postId)
				.orElseThrow(()-> new ResourceNotFoundException("Post", "PostId", postId));
		if(postInputDTO.getPostTitle()!=null) {
			post.setPostTitle(postInputDTO.getPostTitle());
		}
		if(postInputDTO.getPostContent()!=null) {
			post.setPostContent(postInputDTO.getPostContent());
		}
		if(postInputDTO.getImageName()!=null) {
			post.setImageName(postInputDTO.getImageName());
		}
		postRepo.save(post);
		return modelMapper.map(post, PostDTO.class);
	}

	@Override
	public String deletePost(Integer postId) {
		Post post = postRepo.findById(postId)
				.orElseThrow(()-> new ResourceNotFoundException("Post", "PostId", postId));
		postRepo.delete(post);
		return MessageProperties.POST_DELETED.getMessage();
	}

	@Override
	public PostResponse getAllPosts(Integer pageNumber, Integer pageSize, String sortBy, String orderBy) {
		Sort sort = orderBy.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
		Pageable page =PageRequest.of(pageNumber, pageSize, sort);
		Page<Post> pagePost = postRepo.findAll(page);
		List<Post> postList = pagePost.getContent();
		List<PostDTO> postDtoList = postList
				.stream()
				.map(post -> modelMapper.map(post, PostDTO.class))
				.collect(Collectors.toList());

		PostResponse postResponse = new PostResponse();
		postResponse.setPostsList(postDtoList);
		postResponse.setPageNumber(pagePost.getNumber());
		postResponse.setPageSize(pagePost.getSize());
		postResponse.setTotalPages(pagePost.getTotalPages());
		postResponse.setTotalElements(pagePost.getTotalElements());
		postResponse.setLastPage(pagePost.isLast());
		return postResponse;
	}

	@Override
	public PostDTO getPostById(Integer postId) {
		Post post = postRepo.findById(postId)
				.orElseThrow(()-> new ResourceNotFoundException("Post", "PostId", postId));
		return modelMapper.map(post, PostDTO.class);
	}

	@Override
	public List<PostDTO> getAllPostsByCategory(Integer categoryId) {
		Category category = categoryRepo.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
		List<PostDTO> postList = postRepo.findByCategory(category)
				.stream()
				.map(posts -> modelMapper.map(posts, PostDTO.class))
				.collect(Collectors.toList());
		return postList;
	}

	@Override
	public List<PostDTO> getAllPostsByUser(Integer userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(()-> new ResourceNotFoundException("User", "UserId", userId));
		List<PostDTO> postList = postRepo.findByUser(user)
				.stream()
				.map(posts -> modelMapper.map(posts, PostDTO.class))
				.collect(Collectors.toList());
		return postList;
	}

	@Override
	public List<PostDTO> searchPostByTitle(String key) {
		
		List<Post> postList = postRepo.findByPostTitleContaining(key);
		List<PostDTO> postDtoList = postList.stream().map(posts -> modelMapper.map(posts, PostDTO.class))
				.collect(Collectors.toList());
		return postDtoList;
	}

}
