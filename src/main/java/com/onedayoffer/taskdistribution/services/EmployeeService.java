package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import com.onedayoffer.taskdistribution.DTO.TaskStatus;
import com.onedayoffer.taskdistribution.repositories.EmployeeRepository;
import com.onedayoffer.taskdistribution.repositories.TaskRepository;
import com.onedayoffer.taskdistribution.repositories.entities.Employee;
import com.onedayoffer.taskdistribution.repositories.entities.Task;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    public List<EmployeeDTO> getEmployees(@Nullable String sortDirection) {

        List<Employee> employees = new ArrayList<>();

        if (sortDirection != null) {
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            employees = employeeRepository.findAll(Sort.by(direction, "fio"));
        } else {
            employees = employeeRepository.findAll();
        }


        List<EmployeeDTO> employeeDTOS = new ArrayList<>();

        for (Employee employee : employees) {
            EmployeeDTO employeeDTO = new EmployeeDTO(employee.getFio(), employee.getJobTitle());
            employeeDTOS.add(employeeDTO);
        } //TODO  переделай в модель мапер
        return employeeDTOS;
    }

    @Transactional
    public EmployeeDTO getOneEmployee(Integer id) {
        Employee employee = employeeRepository.getOne(id);
        //TODO почему null
        EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
        return employeeDTO;
    }

    public List<TaskDTO> getTasksByEmployeeId(Integer id) {
        Employee employee = employeeRepository.getOne(id);
        List<Task> tasks = employee.getTasks();
        List<TaskDTO> taskDTOS = modelMapper.map(tasks, new TypeToken<List<TaskDTO>>() {}.getType());
        return taskDTOS;
    }

    @Transactional
    public void changeTaskStatus(Integer taskId, TaskStatus status) {

        Task task = taskRepository.findById(taskId).orElseThrow(RuntimeException::new);
        task.setStatus(status);
        taskRepository.save(task);
    }

    @Transactional
    public void postNewTask(Integer employeeId, TaskDTO newTask) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Task task = new Task();
        task.setStatus(newTask.getStatus());
        employee.getTasks().add(task);
        employeeRepository.save(employee);
    }
}
