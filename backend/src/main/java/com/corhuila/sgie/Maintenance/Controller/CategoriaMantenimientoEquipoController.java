package com.corhuila.sgie.Maintenance.Controller;

import com.corhuila.sgie.Maintenance.Entity.CategoriaMantenimientoEquipo;
import com.corhuila.sgie.Maintenance.Service.CategoriaMantenimientoEquipoService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/categoria-mantenimiento-equipo")
public class CategoriaMantenimientoEquipoController extends BaseController<CategoriaMantenimientoEquipo, CategoriaMantenimientoEquipoService> {
    public CategoriaMantenimientoEquipoController(CategoriaMantenimientoEquipoService service) {
        super(service, "CATEGORIA_MANTENIMIENTO_EQUIPO");
    }
}
