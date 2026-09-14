package com.corhuila.sgie.Site.Controller;

import com.corhuila.sgie.Site.Entity.Continente;
import com.corhuila.sgie.Site.IService.IContinenteService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/continente")
public class ContinenteController extends BaseController<Continente, IContinenteService> {
    public ContinenteController(IContinenteService service) {
        super(service, "CONTINENTE");
    }
}
