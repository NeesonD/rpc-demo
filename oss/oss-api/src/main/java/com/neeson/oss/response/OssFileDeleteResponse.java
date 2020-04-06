package com.neeson.oss.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create on 2020-04-03
 *
 * @author Administrator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class OssFileDeleteResponse {

    private boolean success;

}
