package resources;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import util.BookData;
import util.LibraryRoomData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.logging.Logger;

@Path("/subject")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LibraryResource {
    private static final Logger LOG = Logger.getLogger(LibraryResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/addBook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBook(BookData bookData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key bookKey = datastore.newKeyFactory().setKind("Book").newKey(bookData.getId());
            Entity book = txn.get(bookKey);

            if( book != null ) {
                txn.rollback();
                LOG.warning("Book already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Book already exists").build();
            } else {

                book = Entity.newBuilder(bookKey)
                        .set("title", bookData.getTitle())
                        .set("reserved", "FALSE")
                        .set("reservation_date", "")
                        .set("reservation_exp", "")
                        .set("return_date", "")
                        .build();
                txn.add(book);

                LOG.info("Book added in datastore " + bookData.getId());
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/removeBook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeBook(BookData bookData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key bookKey = datastore.newKeyFactory().setKind("Book").newKey(bookData.getId());
            Entity book = txn.get(bookKey);

            if( book == null ) {
                txn.rollback();
                LOG.warning("Book doesn't exists");
                return Response.status(Response.Status.NOT_FOUND).entity("Book doesn't exists").build();
            } else {
                txn.delete(bookKey);

                LOG.info("Book removed from datastore " + bookData.getId());
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PUT
    @Path("/reserveBook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reserveBook(BookData bookData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key bookKey = datastore.newKeyFactory().setKind("Book").newKey(bookData.getId());
            Entity book = txn.get(bookKey);

            if( book == null ) {
                txn.rollback();
                LOG.warning("Book doesn't exists");
                return Response.status(Response.Status.NOT_FOUND).entity("Book doesn't exists").build();
            } else {
                ZoneOffset lisbonOffset = ZoneOffset.of("+01:00");
                LocalDateTime reservationTime = LocalDateTime.now();
                LocalDateTime expirationTime = reservationTime.plusWeeks(1);
                Date reservationDate = Date.from(reservationTime.toInstant(lisbonOffset));
                Date expirationDate = Date.from(expirationTime.toInstant(lisbonOffset));
                Timestamp reservation = Timestamp.of(reservationDate);
                Timestamp expiration = Timestamp.of(expirationDate);

                book = Entity.newBuilder(bookKey, book)
                        .set("reserved", "TRUE")
                        .set("reservation_date", reservation)
                        .set("reservation_exp", expiration)
                        .build();
                txn.put(book);

                LOG.info("Book " + bookData.getId() + " reserved by User ");
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PUT
    @Path("/returnBook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response returnBook(BookData bookData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key bookKey = datastore.newKeyFactory().setKind("Book").newKey(bookData.getId());
            Entity book = txn.get(bookKey);

            if( book == null ) {
                txn.rollback();
                LOG.warning("Book doesn't exists");
                return Response.status(Response.Status.NOT_FOUND).entity("Book doesn't exists").build();
            } else {

                book = Entity.newBuilder(bookKey, book)
                        .set("reserved", "FALSE")
                        .set("return_date", Timestamp.now())
                        .build();
                txn.put(book);

                LOG.info("Book " + bookData.getId() + " returned by User ");
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @POST
    @Path("/addRoom")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRoom(LibraryRoomData roomData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key roomKey = datastore.newKeyFactory().setKind("LibraryRoom").newKey(roomData.getId());
            Entity room = txn.get(roomKey);

            if( room != null ) {
                txn.rollback();
                LOG.warning("Room already exists");
                return Response.status(Response.Status.BAD_REQUEST).entity("Room already exists").build();
            } else {

                room = Entity.newBuilder(roomKey)
                        .set("capacity", roomData.getCapacity())
                        .set("reserved", "FALSE")
                        .set("reservation_date", "")
                        .set("reservation_exp", "")
                        .build();
                txn.add(room);

                LOG.info("Room added in datastore " + roomData.getId());
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @DELETE
    @Path("/removeRoom")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeRoom(LibraryRoomData roomData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key roomKey = datastore.newKeyFactory().setKind("LibraryRoom").newKey(roomData.getId());
            Entity room = txn.get(roomKey);

            if( room == null ) {
                txn.rollback();
                LOG.warning("Room doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Room doesn't exist").build();
            } else {
                txn.delete(roomKey);

                LOG.info("Room removed from datastore " + roomData.getId());
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    @PUT
    @Path("/reserveRoom")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reserveRoom(LibraryRoomData roomData) {

        Transaction txn = datastore.newTransaction();
        try {
            Key roomKey = datastore.newKeyFactory().setKind("LibraryRoom").newKey(roomData.getId());
            Entity room = txn.get(roomKey);

            if( room == null ) {
                txn.rollback();
                LOG.warning("Room doesn't exist");
                return Response.status(Response.Status.NOT_FOUND).entity("Room doesn't exist").build();
            } else {
                ZoneOffset lisbonOffset = ZoneOffset.of("+01:00");
                LocalDateTime reservationTime = LocalDateTime.now();
                LocalDateTime expirationTime = reservationTime.plusWeeks(1);
                Date reservationDate = Date.from(reservationTime.toInstant(lisbonOffset));
                Date expirationDate = Date.from(expirationTime.toInstant(lisbonOffset));
                Timestamp reservation = Timestamp.of(reservationDate);
                Timestamp expiration = Timestamp.of(expirationDate);

                room = Entity.newBuilder(roomKey)
                        .set("capacity", roomData.getCapacity())
                        .set("reserved", "TRUE")
                        .set("reservation_date", reservation)
                        .set("reservation_exp", expiration)
                        .build();
                txn.put(room);

                LOG.info("Room reserved " + roomData.getId());
                txn.commit();
                return Response.ok().build();
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}