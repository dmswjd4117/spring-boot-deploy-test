package com.spring.boot.post.application;import com.spring.boot.common.exception.NotFoundException;import com.spring.boot.image.domain.ImageUploader;import com.spring.boot.image.infrastructure.UploadFile;import com.spring.boot.member.application.MemberService;import com.spring.boot.member.domain.Member;import com.spring.boot.post.application.dto.request.PostCreateRequestDto;import com.spring.boot.post.application.dto.request.PostUpdateRequestDto;import com.spring.boot.post.application.dto.response.PostDto;import com.spring.boot.post.domain.Post;import com.spring.boot.post.infrastructure.PostRepository;import com.spring.boot.tag.application.TagService;import com.spring.boot.tag.domain.Tag;import java.util.Collections;import java.util.List;import java.util.Optional;import java.util.Set;import java.util.stream.Collectors;import lombok.extern.slf4j.Slf4j;import org.springframework.security.access.AccessDeniedException;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.multipart.MultipartFile;@Service@Slf4jpublic class PostService {  private final ImageUploader imageUploader;  private final PostRepository postRepository;  private final TagService tagService;  private final MemberService memberService;  public PostService(ImageUploader imageUploader, PostRepository postRepository,      TagService tagService, MemberService memberService) {    this.imageUploader = imageUploader;    this.postRepository = postRepository;    this.tagService = tagService;    this.memberService = memberService;  }  public Post findByPostId(Long postId) {    return postRepository.findById(postId)        .orElseThrow(() -> new NotFoundException(Post.class, postId));  }  private List<String> uploadAndGetImagePath(List<MultipartFile> multipartFiles) {    if (multipartFiles == null) {      return Collections.emptyList();    }    return multipartFiles.stream()        .map(UploadFile::toUploadFile)        .flatMap(Optional::stream)        .map(imageUploader::upload)        .collect(Collectors.toList());  }  @Transactional  public PostDto updatePost(PostUpdateRequestDto postRequest) {    Long writerId = postRequest.getWriterId();    Long postId = postRequest.getPostId();    List<MultipartFile> multipartFiles = postRequest.getMultipartFiles();    Member writer = memberService.findByMemberId(writerId);    Post post = findByPostId(postId);    if (!post.isWrittenBy(writer)) {      throw new AccessDeniedException("접근권한이 없습니다");    }    List<String> imagePaths = uploadAndGetImagePath(multipartFiles);    post.updatePost(postRequest.getTitle(), postRequest.getContent(),        tagService.saveTags(postRequest.getTagNames()));    post.initImages(imagePaths);    return PostDto.from(post);  }  @Transactional  public Long deletePost(Long writerId, Long postId) {    Member writer = memberService.findByMemberId(writerId);    Post post = postRepository.findById(postId)        .orElseThrow(() -> new NotFoundException(Post.class, postId));    if (!post.isWrittenBy(writer)) {      throw new AccessDeniedException("접근권한이 없습니다");    }    postRepository.deleteById(postId);    return postId;  }  @Transactional  public PostDto createPost(PostCreateRequestDto postRequest) {    Member writer = memberService.findByMemberId(postRequest.getWriterId());    Post post = new Post(postRequest.getTitle(), postRequest.getContent(), writer);    List<String> imagePaths = uploadAndGetImagePath(postRequest.getMultipartFiles());    Set<Tag> tags = tagService.saveTags(postRequest.getTagNames());    post.initImages(imagePaths);    post.initPostTags(tags);    postRepository.save(post);    return PostDto.from(post);  }}