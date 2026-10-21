package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Borrow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {
}
