package com.example.demo.tool;

import com.example.demo.category.CategoryDto;
import com.example.demo.exception.ManufacturerNotFoundException;
import com.example.demo.exception.ToolNotFoundException;
import com.example.demo.exception.UserDontHavePermissionException;
import com.example.demo.handler.GlobalHandler;
import com.example.demo.manufacturer.ManufacturerDto;
import com.example.demo.user.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ToolControllerTest {

    @Mock
    private ToolService toolService;

    @InjectMocks
    private ToolController toolController;

    private MockMvc mockMvc;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private ToolDto firstToolDto;

    private ToolDto secondToolDto;

    private Pageable mockPageable;

    private Page<ToolDto> mockPage;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(toolController)
                .setControllerAdvice(new GlobalHandler())
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
                .photos(List.of("url1", "url2"))
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
                .photos(List.of("url1", "url2"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        mockPageable = PageRequest.of(0, 10);
        mockPage = new PageImpl<>(List.of(firstToolDto, secondToolDto), mockPageable, 2);
    }

    @Test
    public void findAll_returnTwoTools() throws Exception {
        when(toolService.findAll(mockPageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/tools")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].owner.id").value(1))
                .andExpect(jsonPath("$.content[0].manufacturer.id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].manufacturer.id").value(1))
                .andExpect(jsonPath("$.content[1].manufacturer.id").value(1));

        verify(toolService, times(1)).findAll(mockPageable);
    }

    @Test
    public void findMy_returnTwoTools() throws Exception {
        when(toolService.findMy(mockPageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/tools/my")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].owner.id").value(1))
                .andExpect(jsonPath("$.content[0].manufacturer.id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].manufacturer.id").value(1))
                .andExpect(jsonPath("$.content[1].manufacturer.id").value(1));

        verify(toolService, times(1)).findMy(mockPageable);
    }

    @Test
    public void findById_returnTool() throws Exception {
        when(toolService.findById(1L)).thenReturn(firstToolDto);

        mockMvc.perform(get("/api/v1/tools/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(1))
                .andExpect(jsonPath("$.manufacturer.id").value(1))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.type").value("RENT"))
                .andExpect(jsonPath("$.condition").value("NEW"))
                .andExpect(jsonPath("$.price").value(new BigDecimal("3000")))
                .andExpect(jsonPath("$.description").value("Some desc"));

        verify(toolService, times(1)).findById(1L);
    }

    @Test
    public void findById_notFound_returnNotFound() throws Exception {
        when(toolService.findById(1L)).thenThrow(new ToolNotFoundException("Tool with id: 1 not found"));

        mockMvc.perform(get("/api/v1/tools/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tool with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(toolService, times(1)).findById(1L);
    }

    @Test
    public void search_returnTwoTools() throws Exception {
        when(toolService.search("description", 1L, 1L, "RENT", "NEW", new BigDecimal("1000"), new BigDecimal("3000"), mockPageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/tools/search")
                        .param("description", "description")
                        .param("manufacturer", "1")
                        .param("category", "1")
                        .param("type", "RENT")
                        .param("condition", "NEW")
                        .param("gte", "1000")
                        .param("lte", "3000")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].owner.id").value(1))
                .andExpect(jsonPath("$.content[0].manufacturer.id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].manufacturer.id").value(1))
                .andExpect(jsonPath("$.content[1].manufacturer.id").value(1));

        verify(toolService, times(1)).search("description", 1L, 1L, "RENT", "NEW", new BigDecimal("1000"), new BigDecimal("3000"), mockPageable);
    }

    @Test
    public void create_createsTool() throws Exception {
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(1L)
                .categoryId(1L)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .build();
        MockMultipartFile files = new MockMultipartFile("files", "test-content".getBytes());
        MockMultipartFile toolPart = new MockMultipartFile("tool", "tool", "application/json", objectMapper.writeValueAsString(toolCreateUpdateDto).getBytes());
        when(toolService.create(eq(toolCreateUpdateDto), any())).thenReturn(firstToolDto);

        mockMvc.perform(multipart("/api/v1/tools")
                        .file(files)
                        .file(toolPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tools/" + firstToolDto.getId()))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.owner.id").value(1))
                .andExpect(jsonPath("$.manufacturer.id").value(1))
                .andExpect(jsonPath("$.category.id").value(1))
                .andExpect(jsonPath("$.type").value(Type.RENT.name()))
                .andExpect(jsonPath("$.condition").value(Condition.NEW.name()))
                .andExpect(jsonPath("$.price").value(3000))
                .andExpect(jsonPath("$.description").value("Some desc"))
                .andExpect(jsonPath("$.photos[0]").value("url1"))
                .andExpect(jsonPath("$.photos[1]").value("url2"));

        verify(toolService, times(1)).create(eq(toolCreateUpdateDto), any());
    }

    @Test
    public void create_invalidData_returnBadRequest() throws Exception {
        ToolCreateUpdateDto invalidToolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(0L)
                .categoryId(0L)
                .type(null)
                .condition(null)
                .price(new BigDecimal("0.00"))
                .description(null)
                .build();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("files", "test-content".getBytes());
        MockMultipartFile toolPart = new MockMultipartFile("tool", "tool", "application/json", objectMapper.writeValueAsString(invalidToolCreateUpdateDto).getBytes());

        mockMvc.perform(multipart("/api/v1/tools")
                        .file(mockMultipartFile)
                        .file(toolPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.time").exists());
    }

    @Test
    public void create_notFound_returnNotFound() throws Exception {
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(1L)
                .categoryId(1L)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .build();
        MockMultipartFile mockMultipartFile = new MockMultipartFile("files", "test-content".getBytes());
        MockMultipartFile toolPart = new MockMultipartFile("tool", "tool", "application/json", objectMapper.writeValueAsString(toolCreateUpdateDto).getBytes());
        when(toolService.create(eq(toolCreateUpdateDto), any())).thenThrow(new ManufacturerNotFoundException("Manufacturer with this id: 1 not found"));

        mockMvc.perform(multipart("/api/v1/tools")
                        .file(mockMultipartFile)
                        .file(toolPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Manufacturer with this id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(toolService, times(1)).create(eq(toolCreateUpdateDto), any());
    }

    @Test
    public void update_updatesTool() throws Exception {
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(1L)
                .categoryId(1L)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .build();
        List<String> listFilesToDelete = List.of("file1.jpg", "file2.jpg");
        MockMultipartFile files = new MockMultipartFile("files", "test-content".getBytes());
        MockMultipartFile toolPart = new MockMultipartFile("tool", "tool", "application/json", objectMapper.writeValueAsString(toolCreateUpdateDto).getBytes());
        MockMultipartFile filesToDelete = new MockMultipartFile("filesToDelete", "filesToDelete", "application/json", objectMapper.writeValueAsString(listFilesToDelete).getBytes());
        doNothing().when(toolService).update(eq(1L), eq(toolCreateUpdateDto), any(), eq(listFilesToDelete));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/tools/{id}", 1L)
                        .file(files)
                        .file(toolPart)
                        .file(filesToDelete)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNoContent());

        verify(toolService, times(1)).update(eq(1L), eq(toolCreateUpdateDto), any(), eq(listFilesToDelete));
    }

    @Test
    public void update_notFound_returnNotFound() throws Exception {
        ToolCreateUpdateDto toolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(1L)
                .categoryId(1L)
                .type(Type.RENT)
                .condition(Condition.NEW)
                .price(new BigDecimal("3000"))
                .description("Some desc")
                .build();
        List<String> listFilesToDelete = List.of("file1.jpg", "file2.jpg");
        MockMultipartFile files = new MockMultipartFile("files", "test-content".getBytes());
        MockMultipartFile toolPart = new MockMultipartFile("tool", "tool", "application/json", objectMapper.writeValueAsString(toolCreateUpdateDto).getBytes());
        MockMultipartFile filesToDelete = new MockMultipartFile("filesToDelete", "filesToDelete", "application/json", objectMapper.writeValueAsString(listFilesToDelete).getBytes());
        doThrow(new ToolNotFoundException("Tool with this id: 1 not found")).when(toolService).update(eq(1L), eq(toolCreateUpdateDto), any(), eq(listFilesToDelete));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/tools/{id}", 1L)
                        .file(files)
                        .file(toolPart)
                        .file(filesToDelete)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tool with this id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(toolService, times(1)).update(eq(1L), eq(toolCreateUpdateDto), any(), eq(listFilesToDelete));
    }

    @Test
    public void update_invalidData_returnBadRequest() throws Exception {
        ToolCreateUpdateDto invalidToolCreateUpdateDto = ToolCreateUpdateDto.builder()
                .manufacturerId(0L)
                .categoryId(0L)
                .type(null)
                .condition(null)
                .price(new BigDecimal("0.00"))
                .description(null)
                .build();
        List<String> listFilesToDelete = List.of("file1.jpg", "file2.jpg");
        MockMultipartFile files = new MockMultipartFile("files", "test-content".getBytes());
        MockMultipartFile toolPart = new MockMultipartFile("tool", "tool", "application/json", objectMapper.writeValueAsString(invalidToolCreateUpdateDto).getBytes());
        MockMultipartFile filesToDelete = new MockMultipartFile("filesToDelete", "filesToDelete", "application/json", objectMapper.writeValueAsString(listFilesToDelete).getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/tools/{id}", 1L)
                        .file(files)
                        .file(toolPart)
                        .file(filesToDelete)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.time").exists());

    }

    @Test
    public void delete_deleteTool() throws Exception {
        doNothing().when(toolService).delete(1L);

        mockMvc.perform(delete("/api/v1/tools/{id}", 1))
                .andExpect(status().isNoContent());

        verify(toolService, times(1)).delete(1L);
    }

    @Test
    public void delete_notFound_returnNotFound() throws Exception {
        doThrow(new ToolNotFoundException("Tool with id: 1 not found")).when(toolService).delete(1L);

        mockMvc.perform(delete("/api/v1/tools/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tool with id: 1 not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.time").exists());

        verify(toolService, times(1)).delete(1L);
    }

    @Test
    public void delete_dontHavePermission_returnForbidden() throws Exception {
        doThrow(new UserDontHavePermissionException("User with id: 1 cannot perform actions with tool with id: 1")).when(toolService).delete(1L);

        mockMvc.perform(delete("/api/v1/tools/{id}", 1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User with id: 1 cannot perform actions with tool with id: 1"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.time").exists());

        verify(toolService, times(1)).delete(1L);
    }
}