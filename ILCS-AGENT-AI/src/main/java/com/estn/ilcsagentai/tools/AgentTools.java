package com.estn.ilcsagentai.tools;

import com.estn.ilcsagentai.entities.Etudiant;
import com.estn.ilcsagentai.repositories.EudiantRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentTools {
    @Autowired
    private EudiantRepository repo;

    @Tool(description = "récuperer les informations sur un étudiant par son apogee")
    List<Etudiant> getEtudiantParApogee(
            @ToolParam(description = "le apogée de l'étudiant") String apogee) {
        return repo.findByApogee(apogee);
    }

    @Tool(description = "récuperer les informations sur un étudiant par filière")
    List<Etudiant> getEtudiantParFiliere(
            @ToolParam(description = "le nom de la filière") String filiere) {
        return repo.findByFiliere(filiere);
    }

    @Tool(description = "récuperer les informations sur un étudiant par son nom ou prénom")
    List<Etudiant> getEtudiants(
            @ToolParam(description = "nom ou prénom d'un étudiant") String nomOuPrenom) {
        return repo.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(nomOuPrenom, nomOuPrenom);
    }

    @Tool(description = "récuperer les étudiants par niveau (ex: 1A, 2A)")
    List<Etudiant> getEtudiantParNiveau(
            @ToolParam(description = "le niveau de l'étudiant (1A ou 2A)") String niveau) {
        return repo.findByNiveau(niveau);
    }

    @Tool(description = "récuperer les étudiants par filière et niveau")
    List<Etudiant> getEtudiantParFiliereEtNiveau(
            @ToolParam(description = "la filière") String filiere,
            @ToolParam(description = "le niveau (1A ou 2A)") String niveau) {
        return repo.findByFiliereAndNiveau(filiere, niveau);
    }
}
