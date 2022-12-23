package com.spring.boot.post.application;import com.spring.boot.common.exception.NotFoundException;import com.spring.boot.image.domain.ImageUploader;import com.spring.boot.image.infrastructure.UploadFile;import com.spring.boot.member.application.MemberService;import com.spring.boot.member.domain.Member;import com.spring.boot.post.domain.Post;import com.spring.boot.post.infrastructure.PostRepository;import com.spring.boot.post.presentaion.dto.request.PostCreateRequest;import com.spring.boot.post.presentaion.dto.request.PostUpdateRequest;import com.spring.boot.tag.application.TagService;import com.spring.boot.tag.domain.Tag;import java.util.Collections;import java.util.List;import java.util.Optional;import java.util.Set;import java.util.stream.Collectors;import lombok.extern.slf4j.Slf4j;import org.springframework.security.access.AccessDeniedException;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.multipart.MultipartFile;@Service@Slf4jpublic class PostService {  private final ImageUploader imageUploader;  private final PostRepository postRepository;  private final TagService tagService;  private final MemberService memberService;  public PostService(ImageUploader imageUploader, PostRepository postRepository,      TagService tagService, MemberService memberService) {    this.imageUploader = imageUploader;    this.postRepository = postRepository;    this.tagService = tagService;    this.memberService = memberService;  }  private Post findByPostId(Long postId) {    return postRepository.findById(postId)        .orElseThrow(() -> new NotFoundException(Post.class, postId));  }  private List<String> uploadAndGetImagePath(List<MultipartFile> multipartFiles) {    if (multipartFiles == null) {      return Collections.emptyList();    }    return multipartFiles.stream()        .map(UploadFile::toUploadFile)        .flatMap(Optional::stream)        .map(imageUploader::upload)        .collect(Collectors.toList());  }  @Transactional  public Post updatePost(Long writerId, Long postId, PostUpdateRequest postUpdateRequest,      List<MultipartFile> multipartFiles) {    Member writer = memberService.findById(writerId);    Post post = findByPostId(postId);    if (!post.isWrittenBy(writer)) {      throw new AccessDeniedException("접근권한이 없습니다");    }    List<String> imagePaths = uploadAndGetImagePath(multipartFiles);    post.updatePost(postUpdateRequest.getTitle(), postUpdateRequest.getContent(), tagService.createOrGetTags(postUpdateRequest.getTagNames()));    post.initImages(imagePaths);    return post;  }  @Transactional  public Long deletePost(Long writerId, Long postId) {    Member writer = memberService.findById(writerId);    Post post = postRepository.findById(postId)        .orElseThrow(() -> new NotFoundException(Post.class, postId));    if (!post.isWrittenBy(writer)) {      throw new AccessDeniedException("접근권한이 없습니다");    }    postRepository.deleteById(postId);    return postId;  }  @Transactional  public Post createPost(Long writerId, PostCreateRequest postCreateRequest,      List<MultipartFile> multipartFiles) {    Member writer = memberService.findById(writerId);    Post post = new Post(postCreateRequest.getTitle(), postCreateRequest.getContent(), writer);    List<String> imagePaths = uploadAndGetImagePath(multipartFiles);    Set<Tag> tags = tagService.createOrGetTags(postCreateRequest.getTagNames());    post.initImages(imagePaths);    post.initPostTags(tags);    postRepository.save(post);    return post;  }}