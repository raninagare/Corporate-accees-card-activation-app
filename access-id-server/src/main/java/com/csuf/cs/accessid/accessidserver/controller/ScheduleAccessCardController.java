package com.csuf.cs.accessid.accessidserver.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.csuf.cs.accessid.accessidserver.service.IGenerateAccessCardService;
import com.csuf.cs.accessid.accessidserver.util.IAuthUtil;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ScheduleAccessCardController {

	@Autowired
	private IAuthUtil authUtil;

	@Autowired
	private IGenerateAccessCardService generateAccessCardService;

	@PostMapping("/generateid")
	public @ResponseBody ResponseEntity<Map<String, Object>> generateAccessId(
			@RequestHeader("authorization") String jwt, @RequestBody Map<String, Object> payload) {
		try {
			Map<String, Object> response = new HashMap<>();

			String email = payload.get("email").toString();
			long id = Long.valueOf(payload.get("id").toString());
			if (!authUtil.verifyAuthToken(jwt)) {
				response.put("error", "Access token is invalid");
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.UNAUTHORIZED);
			}
			Map<String, Object> idCard = generateAccessCardService.generateAccessCard(payload);
			if (idCard.containsKey("error")) {
				return new ResponseEntity<Map<String, Object>>(idCard, HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<Map<String, Object>>(idCard, HttpStatus.ACCEPTED);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("error", "Something went wrong, please try again!");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/validateemployee")
	public @ResponseBody ResponseEntity<Map<String, Object>> validateEmployeeCard(
			@RequestHeader("authorization") String jwt, @RequestBody Map<String, Object> payload) {
		try {
			Map<String, Object> response = new HashMap<>();
			if (!authUtil.verifyAuthToken(jwt)) {
				response.put("error", "Access token is invalid");
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.UNAUTHORIZED);
			}
			response = generateAccessCardService.validateAccessCard(payload);
			if (response.containsKey("error") || response.containsKey("invalid")) {
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.ACCEPTED);

		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("error", "Failed to validate an employee");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/access-id/{id}")
	public @ResponseBody ResponseEntity<Map<String, Object>> getEmployeeIdDetails(@PathVariable String id,
																	@RequestHeader("authorization") String jwt){
		try {
			if(authUtil.verifyAuthToken(jwt)) {
				long empId = Long.valueOf(id);
				Map<String, Object> idDetails = generateAccessCardService.getEmployeeIdDetails(empId);
				return (!idDetails.containsKey("error"))
						? new ResponseEntity<Map<String, Object>>(idDetails, HttpStatus.ACCEPTED)
						: new ResponseEntity<Map<String, Object>>(idDetails, HttpStatus.BAD_REQUEST);
			} else {
				Map<String, Object> response = new HashMap<>();
				response.put("error", "Access token is invalid");
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.UNAUTHORIZED);
			}
		} catch(Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("error", "Failed to fetch employee id details. Try again in some time.");
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
