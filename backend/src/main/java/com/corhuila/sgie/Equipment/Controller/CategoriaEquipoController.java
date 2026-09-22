package com.corhuila.sgie.Equipment.Controller;

import com.corhuila.sgie.Equipment.Entity.CategoriaEquipo;
import com.corhuila.sgie.Equipment.IService.ICategoriaEquipoService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/categoria-equipo")
public class CategoriaEquipoController extends BaseController<CategoriaEquipo, ICategoriaEquipoService> {
    public CategoriaEquipoController(ICategoriaEquipoService service) {
        super(service, "CATEGORIA_EQUIPO");
    }
}
