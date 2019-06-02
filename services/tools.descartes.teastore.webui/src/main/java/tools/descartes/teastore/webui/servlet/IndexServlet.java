/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.descartes.teastore.webui.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import tools.descartes.research.faasteastorelibrary.api.ITeaStoreFunction;
import tools.descartes.research.faasteastorelibrary.interfaces.persistence.CategoryEntity;
import tools.descartes.teastore.registryclient.Service;
import tools.descartes.teastore.registryclient.loadbalancers.LoadBalancerTimeoutException;
import tools.descartes.teastore.registryclient.rest.LoadBalancedCRUDOperations;
import tools.descartes.teastore.registryclient.rest.LoadBalancedImageOperations;
import tools.descartes.teastore.registryclient.rest.LoadBalancedStoreOperations;
import tools.descartes.teastore.entities.Category;
import tools.descartes.teastore.entities.ImageSizePreset;

/**
 * Servlet implementation for the web view of "Index".
 *
 * @author Andre Bauer
 */
@WebServlet( "/index" )
public class IndexServlet extends AbstractUIServlet
{
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public IndexServlet( )
    {
        super( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleGETRequest( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException, LoadBalancerTimeoutException
    {
        checkforCookie( request, response );
//        request.setAttribute( "CategoryList",
//                LoadBalancedCRUDOperations.getEntities( Service.PERSISTENCE, "categories", Category.class, -1, -1 ) );
        request.setAttribute( "CategoryList", getAllCategories() );
        request.setAttribute( "title", "TeaStore Home" );
        request.setAttribute( "login", LoadBalancedStoreOperations.isLoggedIn( getSessionBlob( request ) ) );
        request.setAttribute( "storeIcon",
                LoadBalancedImageOperations.getWebImage( "icon", ImageSizePreset.ICON.getSize( ) ) );

        request.getRequestDispatcher( "WEB-INF/pages/index.jsp" ).forward( request, response );
    }

    private List<CategoryEntity > getAllCategories(){
        Gson gson = new GsonBuilder( ).setPrettyPrinting( ).create( );

        OkHttpClient client = new OkHttpClient( );

        //TODO wenn kein limit/start angegeben, dann sollen alle categories geladen werden
        Request okHttpRequest =
                new Request.Builder( )
                        .url( "http://10.1.227.142:8080/function/" + ITeaStoreFunction.FN_GET_ALL_CATEGORIES )
                        .get( )
                        .build( );

        com.squareup.okhttp.Response response = null;

        String responseBodyAsString = "";

        try
        {
            response = client.newCall( okHttpRequest ).execute( );

            responseBodyAsString = response.body( ).string( );
        }
        catch ( IOException e )
        {
            e.printStackTrace( );
        }

        List< CategoryEntity > categories = gson.fromJson( responseBodyAsString,
                new TypeToken< List< CategoryEntity > >( ) { }.getType( ) );

        return categories;
    }
}