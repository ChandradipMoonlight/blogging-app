package com.blog.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostInputDTO {
	
	private String postTitle;
	
	
	private String postContent;
	
	private String imageName="default.png";
}
