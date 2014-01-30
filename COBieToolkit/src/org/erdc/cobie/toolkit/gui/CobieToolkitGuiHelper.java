package org.erdc.cobie.toolkit.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import org.bimserver.interfaces.objects.SProject;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.shared.interfaces.bimsie1.Bimsie1ServiceInterface;

public class CobieToolkitGuiHelper
{
    public static List<SProject> getSubProjectsFromOids(List<Long> projectOids, Bimsie1ServiceInterface service)
    {
        ArrayList<SProject> projects = new ArrayList<SProject>();
        for (long oid : projectOids)
        {
            try
            {
                SProject project = service.getProjectByPoid(oid);
                projects.add(project);
            } catch (ServerException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UserException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return projects;
    }

    public static List<SProject> getTopLevelProjects(Bimsie1ServiceInterface serviceInterface) throws ServerException, UserException
    {
        List<SProject> projects = serviceInterface.getAllProjects(true, true);
        return projects;
    }

    public static void populateRevisionTable(JTable table, Bimsie1ServiceInterface serviceInterface, SProject project)
    {
        for (long revisionId : project.getRevisions())
        {
            try
            {
                serviceInterface.getRevision(revisionId);

            } catch (ServerException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UserException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
