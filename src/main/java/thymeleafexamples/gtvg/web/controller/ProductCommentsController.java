/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package thymeleafexamples.gtvg.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import thymeleafexamples.gtvg.business.entities.Product;
import thymeleafexamples.gtvg.business.services.ProductService;

@Controller
@RequiredArgsConstructor
public class ProductCommentsController {

    private final ProductService productService;

    @GetMapping("/product/comments")
    public String showProductComments(
            @RequestParam("prodId") final Integer prodId,
            final Model model) {
        final Product product = this.productService.findById(prodId);
        model.addAttribute("prod", product);
        return "product/comments";
    }

}
