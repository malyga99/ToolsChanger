package com.example.demo.tool;

import com.example.demo.category.Category;
import com.example.demo.category.CategoryDto;
import com.example.demo.category.CategoryService;
import com.example.demo.elasticsearch.ElasticService;
import com.example.demo.event.ToolCreatedEvent;
import com.example.demo.event.ToolDeletedEvent;
import com.example.demo.event.ToolUpdatedEvent;
import com.example.demo.exception.ToolNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.image.ImageService;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerDto;
import com.example.demo.manufacturer.ManufacturerService;
import com.example.demo.user.User;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ManufacturerService manufacturerService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ImageService imageService;

    @Mock
    private ToolMapper toolMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private ElasticService elasticService;

    @InjectMocks
    private ToolServiceImpl toolService;

    private ToolDto firstToolDto;

    private ToolDto secondToolDto;

    private ToolCreateUpdateDto toolCreateUpdateDto;

    private Tool firstTool;

    private Tool secondTool;

    private List<MultipartFile> files;

    private List<String> fileNames;

    private MultipartFile file;

    private User user;

    private Manufacturer manufacturer;

    private Category category;

    private Pageable mockPageable;

    private Page<Tool> mockPage;


    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).build();
        manufacturer = Manufacturer.builder().build();
        category = Category.builder().build();

        file = mock(MultipartFile.class);
        files = List.of(file);
        fileNames = List.of("file1.jpg");

        toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(1L)
                .categoryId(1L)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .build();
        firstTool = Tool.builder()
                .id(1L)
                .owner(user)
                .manufacturer(manufacturer)
                .category(category)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(List.of("test-file1.jpg", "test-file2.jpg"))
                .build();
        secondTool = Tool.builder()
                .id(2L)
                .owner(user)
                .manufacturer(manufacturer)
                .category(category)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(List.of("test-file1.jpg", "test-file2.jpg"))
                .build();
        firstToolDto = ToolDto.builder()
                .id(1L)
                .owner(UserDto.builder()
                        .id(1L)
                        .firstname("Ivan")
                        .lastname("Ivanov")
                        .build())
                .manufacturer(ManufacturerDto.builder()
                        .id(1L)
                        .name("Makita")
                        .build())
                .category(CategoryDto.builder()
                        .id(1L)
                        .name("Hammer")
                        .build())
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(List.of("test-file1.jpg", "test-file2.jpg"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        secondToolDto = ToolDto.builder()
                .id(2L)
                .owner(UserDto.builder()
                        .id(1L)
                        .firstname("Ivan")
                        .lastname("Ivanov")
                        .build())
                .manufacturer(ManufacturerDto.builder()
                        .id(1L)
                        .name("Makita")
                        .build())
                .category(CategoryDto.builder()
                        .id(1L)
                        .name("Hammer")
                        .build())
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(List.of("test-file1.jpg", "test-file2.jpg"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstTool, secondTool), mockPageable, 2);
    }

    @Test
    public void findAll_returnTwoTools() {
        when(toolRepository.findAll(mockPageable)).thenReturn(mockPage);
        when(toolMapper.toDto(firstTool)).thenReturn(firstToolDto);
        when(toolMapper.toDto(secondTool)).thenReturn(secondToolDto);

        Page<ToolDto> result = toolService.findAll(mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstToolDto, result.getContent().get(0));
        assertEquals(secondToolDto, result.getContent().get(1));

        verify(toolRepository, times(1)).findAll(mockPageable);
        verify(toolMapper, times(1)).toDto(firstTool);
        verify(toolMapper, times(1)).toDto(secondTool);
    }

    @Test
    public void findMy_returnTwoTools() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(toolRepository.findByOwner(mockPageable, user)).thenReturn(mockPage);
        when(toolMapper.toDto(firstTool)).thenReturn(firstToolDto);
        when(toolMapper.toDto(secondTool)).thenReturn(secondToolDto);

        Page<ToolDto> result = toolService.findMy(mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstToolDto, result.getContent().get(0));
        assertEquals(secondToolDto, result.getContent().get(1));

        verify(userService, times(1)).getCurrentUser();
        verify(toolRepository, times(1)).findByOwner(mockPageable, user);
        verify(toolMapper, times(1)).toDto(firstTool);
        verify(toolMapper, times(1)).toDto(secondTool);
    }

    @Test
    public void findById_returnTool() {
        when(toolRepository.findById(1L)).thenReturn(Optional.of(firstTool));
        when(toolMapper.toDto(firstTool)).thenReturn(firstToolDto);

        ToolDto result = toolService.findById(1L);
        assertNotNull(result);
        assertEquals(firstToolDto, result);

        verify(toolRepository, times(1)).findById(1L);
        verify(toolMapper, times(1)).toDto(firstTool);
    }

    @Test
    public void findById_notFound_throwExc() {
        when(toolRepository.findById(1L)).thenReturn(Optional.empty());

        ToolNotFoundException toolNotFoundException = assertThrows(ToolNotFoundException.class, () -> toolService.findById(1L));
        assertEquals("Tool with id: 1 not found", toolNotFoundException.getMessage());

        verify(toolRepository, times(1)).findById(1L);
    }

    @Test
    public void search_returnTwoTools() {
        List<Long> ids = List.of(1L, 2L);
        when(elasticService.search("description", 1L, 1L, "RENT", "NEW", new BigDecimal("1000"), new BigDecimal("3000"))).thenReturn(ids);
        when(toolRepository.findAllByIdIn(ids, mockPageable)).thenReturn(mockPage);
        when(toolMapper.toDto(firstTool)).thenReturn(firstToolDto);
        when(toolMapper.toDto(secondTool)).thenReturn(secondToolDto);

        Page<ToolDto> result = toolService.search("description", 1L, 1L, "RENT", "NEW", new BigDecimal("1000"), new BigDecimal("3000"), mockPageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(firstToolDto, result.getContent().get(0));
        assertEquals(secondToolDto, result.getContent().get(1));

        verify(elasticService, times(1)).search("description", 1L, 1L, "RENT", "NEW", new BigDecimal("1000"), new BigDecimal("3000"));
        verify(toolRepository, times(1)).findAllByIdIn(ids, mockPageable);
        verify(toolMapper, times(1)).toDto(firstTool);
        verify(toolMapper, times(1)).toDto(secondTool);
    }

    @Test
    public void create_returnCreatedTool() {
        Tool savedTool = Tool.builder()
                .id(1L)
                .owner(user)
                .manufacturer(manufacturer)
                .category(category)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(List.of("test-file1.jpg", "test-file2.jpg"))
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        when(manufacturerService.findById(1L)).thenReturn(manufacturer);
        when(categoryService.findById(1L)).thenReturn(category);
        when(imageService.processFiles(files)).thenReturn(fileNames);
        when(toolMapper.toEntity(eq(toolCreateUpdateDto), eq(user), eq(manufacturer), eq(category), eq(fileNames), any(LocalDateTime.class))).thenReturn(firstTool);
        when(toolRepository.save(firstTool)).thenReturn(savedTool);
        when(toolMapper.toDto(savedTool)).thenReturn(firstToolDto);

        ToolDto result = toolService.create(toolCreateUpdateDto, files);

        assertNotNull(result);
        assertEquals(firstToolDto, result);

        verify(eventPublisher, times(1)).publishEvent(ToolCreatedEvent.builder().createdTool(savedTool).build());
        verify(userService, times(1)).getCurrentUser();
        verify(manufacturerService, times(1)).findById(1L);
        verify(categoryService, times(1)).findById(1L);
        verify(imageService, times(1)).processFiles(files   );
        verify(toolMapper, times(1)).toEntity(eq(toolCreateUpdateDto), eq(user), eq(manufacturer), eq(category), eq(fileNames), any(LocalDateTime.class));
        verify(toolRepository, times(1)).save(firstTool);
        verify(toolMapper, times(1)).toDto(savedTool);
    }

    @Test
    public void delete_deleteTool() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(firstTool));
        doNothing().when(imageService).deleteFiles(firstTool.getPhotos());

        toolService.delete(1L);

        verify(eventPublisher, times(1)).publishEvent(ToolDeletedEvent.builder().toolId(1L).build());
        verify(userService, times(1)).getCurrentUser();
        verify(toolRepository, times(1)).findById(1L);
        verify(toolRepository, times(1)).delete(firstTool);
        verify(imageService, times(1)).deleteFiles(firstTool.getPhotos());
    }

    @Test
    public void delete_toolNotFound_throwExc() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(toolRepository.findById(1L)).thenReturn(Optional.empty());

        ToolNotFoundException toolNotFoundException = assertThrows(ToolNotFoundException.class, () -> toolService.delete(1L));

        assertEquals("Tool with id: 1 not found", toolNotFoundException.getMessage());

        verify(eventPublisher, never()).publishEvent(any());
        verify(userService, times(1)).getCurrentUser();
        verify(toolRepository, times(1)).findById(1L);
        verify(toolRepository, never()).delete(any(Tool.class));
    }

    @Test
    public void delete_userDoesNotHaveRights_throwExc() {
        Tool tool = Tool.builder()
                .id(1L)
                .owner(User.builder().id(3L).build())
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        when(toolRepository.findById(1L)).thenReturn(Optional.of(tool));

        UserDontHavePermissionException userDontHavePermissionException = assertThrows(UserDontHavePermissionException.class, () -> toolService.delete(1L));

        assertEquals("User with id: 1 cannot perform actions with tool with id: 1", userDontHavePermissionException.getMessage());

        verify(eventPublisher, never()).publishEvent(any());
        verify(userService, times(1)).getCurrentUser();
        verify(toolRepository, times(1)).findById(1L);
        verify(toolRepository, never()).delete(any(Tool.class));
    }

    @Test
    public void update_updateTool() {
        Tool existingTool = Tool.builder()
                .id(1L)
                .owner(user)
                .manufacturer(manufacturer)
                .category(category)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(new ArrayList<>(List.of("test-file1.jpg", "test-file2.jpg")))
                .build();
        List<String> filesToDelete = List.of("first-file.jpg", "second-file.jpg");
        when(toolRepository.findById(1L)).thenReturn(Optional.of(existingTool));
        when(userService.getCurrentUser()).thenReturn(user);
        when(manufacturerService.findById(1L)).thenReturn(manufacturer);
        when(categoryService.findById(1L)).thenReturn(category);
        doNothing().when(imageService).deleteFiles(filesToDelete);
        when(imageService.processFiles(files)).thenReturn(List.of("first-file.jpg"));
        when(toolMapper.toEntity(eq(toolCreateUpdateDto), eq(user), eq(manufacturer), eq(category), eq(existingTool.getPhotos()), any())).thenReturn(firstTool);
        when(toolRepository.save(firstTool)).thenReturn(firstTool);

        toolService.update(1L, toolCreateUpdateDto, files, filesToDelete);

        assertEquals(List.of("test-file1.jpg", "test-file2.jpg", "first-file.jpg"), existingTool.getPhotos());
        assertEquals(existingTool.getId(), firstTool.getId());
        assertEquals(existingTool.getCreatedAt(), firstTool.getCreatedAt());


        verify(eventPublisher, times(1)).publishEvent(ToolUpdatedEvent.builder().updatedTool(firstTool).build());
        verify(toolRepository, times(1)).findById(1L);
        verify(userService, times(1)).getCurrentUser();
        verify(manufacturerService, times(1)).findById(1L);
        verify(categoryService, times(1)).findById(1L);
        verify(imageService, times(1)).deleteFiles(filesToDelete);
        verify(imageService, times(1)).processFiles(files);
        verify(toolMapper, times(1)).toEntity(eq(toolCreateUpdateDto), eq(user), eq(manufacturer), eq(category), eq(existingTool.getPhotos()), any());
        verify(toolRepository, times(1)).save(firstTool);
    }

    @Test
    public void update_removeDeletedFilesCorrectly() {
        Tool existingTool = Tool.builder()
                .id(1L)
                .owner(user)
                .manufacturer(manufacturer)
                .category(category)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .photos(new ArrayList<>(List.of("test-file1.jpg", "test-file2.jpg")))
                .build();
        List<String> filesToDelete = List.of("test-file1.jpg", "test-file2.jpg");
        when(toolRepository.findById(1L)).thenReturn(Optional.of(existingTool));
        when(userService.getCurrentUser()).thenReturn(user);
        when(manufacturerService.findById(1L)).thenReturn(manufacturer);
        when(categoryService.findById(1L)).thenReturn(category);
        doNothing().when(imageService).deleteFiles(filesToDelete);
        when(imageService.processFiles(files)).thenReturn(List.of("first-file.jpg"));
        when(toolMapper.toEntity(eq(toolCreateUpdateDto), eq(user), eq(manufacturer), eq(category), eq(existingTool.getPhotos()), any())).thenReturn(firstTool);

        toolService.update(1L, toolCreateUpdateDto, files, filesToDelete);

        assertEquals(List.of("first-file.jpg"), existingTool.getPhotos());
    }

    @Test
    public void update_toolNotFound_throwExc() {
        when(toolRepository.findById(1L)).thenReturn(Optional.empty());

        ToolNotFoundException toolNotFoundException = assertThrows(ToolNotFoundException.class, () -> toolService.update(1L, toolCreateUpdateDto, files, List.of()));
        assertEquals("Tool with id: 1 not found", toolNotFoundException.getMessage());

        verify(eventPublisher, never()).publishEvent(any());
        verify(toolRepository, times(1)).findById(1L);
        verify(toolRepository, never()).save(any(Tool.class));
    }
}