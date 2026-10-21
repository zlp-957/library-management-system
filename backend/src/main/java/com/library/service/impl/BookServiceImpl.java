package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.entity.Book;
import com.library.entity.BookType;
import com.library.mapper.BookMapper;
import com.library.mapper.BookTypeMapper;
import com.library.service.BookService;
import com.library.util.FileUtil;
import com.library.vo.BookVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书服务实现
 */
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookTypeMapper bookTypeMapper;

    @Autowired
    private FileUtil fileUtil;

    @Override
    public Page<BookVO> pageBooks(Integer page, Integer size, String keyword, Long typeId) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getName, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getIsbn, keyword));
        }
        if (typeId != null) {
            wrapper.eq(Book::getTypeId, typeId);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        Page<Book> bookPage = bookMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量查询分类名
        List<Long> typeIds = bookPage.getRecords().stream()
                .map(Book::getTypeId).distinct().collect(Collectors.toList());
        Map<Long, String> typeNameMap = typeIds.isEmpty() ? Map.of() :
                bookTypeMapper.selectBatchIds(typeIds).stream()
                        .collect(Collectors.toMap(BookType::getId, BookType::getName));

        Page<BookVO> voPage = new Page<>(page, size, bookPage.getTotal());
        List<BookVO> voList = bookPage.getRecords().stream().map(b -> {
            BookVO vo = new BookVO();
            BeanUtils.copyProperties(b, vo);
            vo.setTypeName(typeNameMap.getOrDefault(b.getTypeId(), "未分类"));
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public BookVO getById(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        BookVO vo = new BookVO();
        BeanUtils.copyProperties(book, vo);
        if (book.getTypeId() != null) {
            BookType type = bookTypeMapper.selectById(book.getTypeId());
            vo.setTypeName(type == null ? "未分类" : type.getName());
        }
        return vo;
    }

    @Override
    public void add(Book book) {
        if (!StringUtils.hasText(book.getName())) {
            throw new BusinessException("书名不能为空");
        }
        if (book.getPrice() == null || book.getPrice().signum() < 0) {
            throw new BusinessException("售价不能为负");
        }
        if (book.getStock() == null || book.getStock() < 0) {
            book.setStock(0);
        }
        bookMapper.insert(book);
    }

    @Override
    public void update(Book book) {
        if (book.getId() == null) {
            throw new BusinessException("图书id不能为空");
        }
        if (book.getStock() != null && book.getStock() < 0) {
            throw new BusinessException("库存不能为负");
        }
        bookMapper.updateById(book);
    }

    @Override
    public void delete(Long id) {
        bookMapper.deleteById(id);
    }

    @Override
    public String uploadCover(MultipartFile file) {
        return fileUtil.saveCover(file);
    }
}
