

package com.example.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nombre;
    private String correo;
    private String password;
    private String tipoUsuario;

    public Usuario() {
    }

    public Usuario(String nombre, String correo,
                   String password, String tipoUsuario) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.tipoUsuario = tipoUsuario;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getPassword() {
        return password;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
}
