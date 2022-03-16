package petclinic.api.client.owner;

import org.fedex.test.common.api.rest.ApiRequest;
import org.fedex.test.common.api.rest.ApiResponse;
import org.fedex.test.common.api.rest.FedexApiClient;
import org.fedex.test.common.api.rest.exception.InvalidResponseException;
import com.google.gson.GsonBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.internal.mapping.GsonMapper;
import io.restassured.mapper.ObjectMapperType;
import petclinic.api.client.owner.datamodel.Owner;

import java.util.Arrays;
import java.util.List;

/**
 * Client for PetClinic Owner endpoint
 */
public class OwnerApiClient extends FedexApiClient 
{
  /**
   * Create an instance of Owner API Client
   * 
   * @param baseUrl base url of the PetClinic API
   */
  public OwnerApiClient(String baseUrl)
  {
    super(baseUrl, "/api/owners");
    ObjectMapperConfig config = new ObjectMapperConfig(ObjectMapperType.GSON)
                                    .gsonObjectMapperFactory((type, s) -> 
                                    new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create());
    setObjectMapper(new GsonMapper(config.gsonObjectMapperFactory()));
  }

  /**
   * Get all the owners
   *
   * @return List of Owners
   * @throws InvalidResponseException Generic Fedex API Response Exception
   */
  public List<Owner> getOwners() throws InvalidResponseException
  {
    logger.info("Get all the owners");
    ApiResponse<Owner[]> response = caller.executeRequest(createRequest(), Method.GET, Owner[].class);
    return Arrays.asList(response.getContent());
  }

  /**
   * Create an Owner
   *
   * @param owner Instance of an Owner that we want to create
   * @return Instance of an owner with an id
   * @throws OwnerApiClientException Exception specific for the Owner API endpoint
   */
  public Owner createOwner(Owner owner) throws OwnerApiClientException
  {
    try 
    {
      logger.info("Creating a new owner");
      ApiRequest request = createRequest().withBody(owner)
                               .withHeader("Content-Type", ContentType.JSON.toString());
      ApiResponse<Owner> response = caller.executeRequest(request, Method.POST, Owner.class);
      return response.getContent();
    }
    catch (InvalidResponseException ex)
    {
      throw new OwnerApiClientException("Unable to create an Owner", owner, ex);
    }
  }

  /**
   * Get an Owner by its identifier
   *
   * @param ownerId Identifier of the owner
   * @return Instance on a Owner for the owner identifier
   * @throws InvalidResponseException Generic Fedex API Response Exception
   */
  public Owner getOwner(Integer ownerId) throws InvalidResponseException
  {
    logger.info("Getting details for Owner with id {}", ownerId);
    return caller.executeRequest(createRequest().withBasePath("/" + ownerId),
        Method.GET, Owner.class).getContent();
  }

  /**
   * Delete an Owner
   *
   * @param ownerId Identifier of the owner
   * @return Response Status code
   */
  public Integer deleteOwner(Integer ownerId) {
    logger.info("Deleting Owner with id {}", ownerId);
    return caller.executeRequest(createRequest().withBasePath("/" + ownerId),
        Method.DELETE, String.class).getHttpStatusCode();
  }

  /**
   * Update an Owner
   *
   * @param owner Instance of an Owner that we want to update
   * @return Response status code
   */
  public Integer updateOwner(Owner owner) {
    logger.info("Update Owner with id {}", owner.getId());
    ApiRequest request = createRequest().withBasePath("/" + owner.getId()).withBody(owner)
                             .withHeader("Content-Type", "application/json");
    return caller.executeRequest(request, Method.PUT, Owner.class).getHttpStatusCode();
  }
}
