package com.example.demo.initializer;

import com.example.demo.category.Category;
import com.example.demo.category.CategoryRepository;
import com.example.demo.manufacturer.Manufacturer;
import com.example.demo.manufacturer.ManufacturerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ManufacturerRepository manufacturerRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeCategory();
        initializeManufacturer();
    }

    private void initializeCategory() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder().name("Молоток").build(),
                    Category.builder().name("Дрель").build(),
                    Category.builder().name("Шуруповерт").build(),
                    Category.builder().name("Перфоратор").build(),
                    Category.builder().name("Лобзик").build(),
                    Category.builder().name("Болгарка (УШМ)").build(),
                    Category.builder().name("Рубанок").build(),
                    Category.builder().name("Фрезер").build(),
                    Category.builder().name("Пила циркулярная").build(),
                    Category.builder().name("Лазерный уровень").build(),
                    Category.builder().name("Компрессор").build(),
                    Category.builder().name("Гвоздезабивной пистолет").build(),
                    Category.builder().name("Отвертка аккумуляторная").build(),
                    Category.builder().name("Точило").build(),
                    Category.builder().name("Газовый паяльник").build(),
                    Category.builder().name("Сварочный аппарат").build(),
                    Category.builder().name("Степлер строительный").build(),
                    Category.builder().name("Клеевой пистолет").build(),
                    Category.builder().name("Электрический рубанок").build(),
                    Category.builder().name("Миксер строительный").build()
            );
            categoryRepository.saveAll(categories);
        }
    }

    private void initializeManufacturer() {
        if (manufacturerRepository.count() == 0) {
            List<Manufacturer> manufacturers = List.of(
                    Manufacturer.builder().name("Bosch").build(),
                    Manufacturer.builder().name("Makita").build(),
                    Manufacturer.builder().name("DeWalt").build(),
                    Manufacturer.builder().name("Metabo").build(),
                    Manufacturer.builder().name("Milwaukee").build(),
                    Manufacturer.builder().name("Ryobi").build(),
                    Manufacturer.builder().name("AEG").build(),
                    Manufacturer.builder().name("Hitachi (HiKOKI)").build(),
                    Manufacturer.builder().name("Stanley").build(),
                    Manufacturer.builder().name("Black+Decker").build(),
                    Manufacturer.builder().name("Интерскол").build(),
                    Manufacturer.builder().name("Зубр").build(),
                    Manufacturer.builder().name("Ресанта").build(),
                    Manufacturer.builder().name("Калибр").build(),
                    Manufacturer.builder().name("Dremel").build(),
                    Manufacturer.builder().name("Hilti").build(),
                    Manufacturer.builder().name("Festool").build(),
                    Manufacturer.builder().name("Einhell").build(),
                    Manufacturer.builder().name("Sturm!").build(),
                    Manufacturer.builder().name("Patriot").build()
            );
            manufacturerRepository.saveAll(manufacturers);
        }
    }
}
