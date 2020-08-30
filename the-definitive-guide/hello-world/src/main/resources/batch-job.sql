delete from batch_job_seq;
delete from batch_job_execution_seq;
delete from batch_step_execution_seq;

delete from batch_step_execution_context;
delete from batch_step_execution;

delete from batch_job_execution_context;
delete from batch_job_execution_params;
delete from batch_job_execution;
delete from batch_job_instance;

select * from batch_job_instance;
select * from batch_job_execution;
select * from batch_job_execution_params;
select * from batch_job_execution_context;

select * from batch_step_execution;
select * from batch_step_execution_context;