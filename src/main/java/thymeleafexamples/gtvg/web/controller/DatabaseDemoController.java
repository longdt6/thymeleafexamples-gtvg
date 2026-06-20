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
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;
import thymeleafexamples.gtvg.business.services.DatabaseDemoService;

@Controller
@RequiredArgsConstructor
public class DatabaseDemoController {

    private final DatabaseDemoService databaseDemoService;

    @GetMapping("/db-demo")
    public String showDatabaseDemo(final Model model) {
        final DatabaseDemoStatus dbStatus = this.databaseDemoService.getStatus();
        model.addAttribute("dbStatus", dbStatus);
        return "dbdemo";

    }

}
