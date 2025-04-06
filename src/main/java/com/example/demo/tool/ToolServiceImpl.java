package com.example.demo.tool;

import com.example.demo.category.Category;
import com.example.demo.category.CategoryService;
import com.example.demo.elasticsearch.ElasticService;
import com.example.demo.event.ToolCreatedEvent;
import com.example.demo.event.ToolDeletedEvent;
import com.example.demo.event.ToolUpdatedEvent;
import com.example.demo.exception.ToolNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.image.ImageService;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerService;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final UserService userService;
    private final ManufacturerService manufacturerService;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final ToolMapper toolMapper;
    private final ToolRepository toolRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ElasticService elasticService;
    private final Logger LOGGER = LoggerFactory.getLogger(ToolServiceImpl.class);

    @Override
    public Page<ToolDto> findAll(Pageable pageable) {
        LOGGER.debug("findAll: Fetching tools - pageNumber: {}, pageSize: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Tool> tools = toolRepository.findAll(pageable);

        LOGGER.debug("findAll: Fetched {} tools", tools.getContent().size());
        return tools.map(toolMapper::toDto);
    }

    @Override
    public Page<ToolDto> findMy(Pageable pageable) {
        LOGGER.debug("findMy: Fetching tools of the current user - pageNumber: {}, pageSize: {}", pageable.getPageNumber(), pageable.getPageSize());

        User currentUser = userService.getCurrentUser();
        Page<Tool> tools = toolRepository.findByOwner(pageable, currentUser);

        LOGGER.debug("findMy: Fetched {} tools of the current user: {}", tools.getContent().size(), currentUser.getLogin());
        return tools.map(toolMapper::toDto);
    }

    @Override
    public ToolDto findById(Long id) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new ToolNotFoundException("Tool with id: " + id + " not found"));
        LOGGER.debug("findById: Fetched tool by ID: {}", id);
        return toolMapper.toDto(tool);
    }

    @Override
    public Page<ToolDto> search(String description, Long manufacturer, Long category, String type, String condition, BigDecimal gte, BigDecimal lte, Pageable pageable) {
        LOGGER.debug("search: Fetching tools with filters - description: {}, manufacturer: {}, category: {}, type: {}, condition: {}, price range: {} - {}. pageNumber: {}, pageSize: {}",
                description, manufacturer, category, type, condition, gte, lte, pageable.getPageNumber(), pageable.getPageSize());

        List<Long> toolsIds = elasticService.search(description, manufacturer, category, type, condition, gte, lte);
        Page<Tool> tools = toolRepository.findAllByIdIn(toolsIds, pageable);

        LOGGER.debug("search: Fetched: {} tools with filters", tools.getContent().size());
        return tools.map(toolMapper::toDto);
    }

    @Override
    @Transactional
    public ToolDto create(ToolCreateUpdateDto toolCreateUpdateDto, List<MultipartFile> files) {
        LOGGER.debug("create: Creating a new tool with data: {}", toolCreateUpdateDto);

        User owner = userService.getCurrentUser();
        Manufacturer manufacturer = manufacturerService.findById(toolCreateUpdateDto.getManufacturerId());
        Category category = categoryService.findById(toolCreateUpdateDto.getCategoryId());
        List<String> fileNames = imageService.processFiles(files);

        Tool tool = toolMapper.toEntity(toolCreateUpdateDto, owner, manufacturer, category, fileNames, LocalDateTime.now());
        Tool savedTool = toolRepository.save(tool);
        LOGGER.debug("create: Successfully created tool with id: {}", savedTool.getId());

        eventPublisher.publishEvent(ToolCreatedEvent.builder()
                .createdTool(savedTool)
                .build());
        return toolMapper.toDto(savedTool);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        LOGGER.debug("delete: Deleting tool with id: {}", id);

        User currentUser = userService.getCurrentUser();
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new ToolNotFoundException("Tool with id: " + id + " not found"));
        checkUserRights(tool, currentUser);

        imageService.deleteFiles(tool.getPhotos());
        toolRepository.delete(tool);

        LOGGER.debug("delete: Successfully deleted tool with id: {}", id);
        eventPublisher.publishEvent(ToolDeletedEvent.builder()
                .toolId(id)
                .build());
    }

    @Override
    @Transactional
    public void update(Long id, ToolCreateUpdateDto toolCreateUpdateDto, List<MultipartFile> files, List<String> filesToDelete) {
        LOGGER.debug("update: Updating tool with id: {}", id);
        Tool existingTool = toolRepository.findById(id)
                .orElseThrow(() -> new ToolNotFoundException("Tool with id: " + id + " not found"));

        User currentUser = userService.getCurrentUser();
        Manufacturer manufacturer = manufacturerService.findById(toolCreateUpdateDto.getManufacturerId());
        Category category = categoryService.findById(toolCreateUpdateDto.getCategoryId());
        checkUserRights(existingTool, currentUser);

        imageService.deleteFiles(filesToDelete);
        List<String> fileNames = removeDeletedFiles(existingTool.getPhotos(), filesToDelete);
        LOGGER.debug("update: Remove deleted files: {}", fileNames);

        fileNames.addAll(imageService.processFiles(files));

        Tool tool = toolMapper.toEntity(toolCreateUpdateDto, currentUser, manufacturer, category, fileNames, LocalDateTime.now());
        tool.setId(existingTool.getId());
        tool.setCreatedAt(existingTool.getCreatedAt());
        Tool savedTool = toolRepository.save(tool);

        LOGGER.info("update: Successfully updated tool with id: {}", id);

        eventPublisher.publishEvent(ToolUpdatedEvent.builder()
                .updatedTool(savedTool)
                .build());

    }

    private void checkUserRights(Tool tool, User user) {
        if (!tool.getOwner().getId().equals(user.getId())) {
            throw new UserDontHavePermissionException("User with id: " + user.getId() + " cannot perform actions with tool with id: " + tool.getId());
        }
    }

    private List<String> removeDeletedFiles(List<String> files, List<String> deletedFiles) {
        deletedFiles.forEach(el -> files.remove(el));
        return files;
    }

}
