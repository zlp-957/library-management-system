package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.common.BusinessException;
import com.library.entity.BookType;
import com.library.mapper.BookTypeMapper;
import com.library.service.BookTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 图书分类服务实现
 */
@Service
public class BookTypeServiceImpl implements BookTypeService {

    @Autowired
    private BookTypeMapper bookTypeMapper;

    @Override
    public List<BookType> listAll() {
        return bookTypeMapper.selectList(new LambdaQueryWrapper<BookType>()
                .orderByAsc(BookType::getId));
    }

    @Override
    public void add(BookType bookType) {
        if (!StringUtils.hasText(bookType.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        Long count = bookTypeMapper.selectCount(new LambdaQueryWrapper<BookType>()
                .eq(BookType::getName, bookType.getName()));
        if (count != null && count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        bookTypeMapper.insert(bookType);
    }

    @Override
    public void update(BookType bookType) {
        if (bookType.getId() == null) {
            throw new BusinessException("分类id不能为空");
        }
        bookTypeMapper.updateById(bookType);
    }

    @Override
    public void delete(Long id) {
        bookTypeMapper.deleteById(id);
    }
}
