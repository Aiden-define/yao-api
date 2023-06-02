package com.yao.project.common;

import lombok.Data;

import java.io.Serializable;

/**
 * id封装
 *
 * @author DH
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
